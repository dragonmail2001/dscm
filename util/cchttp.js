/*
 * Copyright 2015-2115 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @email   dragonmail2001@163.com
 * @author  jinglong.zhaijl
 * @date    2015-10-24
 *
 */
var fs = require('fs'); 
var ftp = require('ftp');
var path = require('path');
var http = require('http');
var util = require('util');
var async = require('async');
var crypto = require('crypto');
var buffer = require("buffer");
var qs = require('querystring');
var conf = require('../configure');
var logger = require('../logger.js');
var ccutil = require('../util/ccutil.js');

function httpNext(otpl, type, back, next, req, res, curr) {
    var calls = []; next.forEach(function(itf){
        if(itf.dtpl != null) {
            if(!ccutil.loadPara(itf.dtpl, itf.uuid, req)){
                return;
            }
        }
          
        req[conf.max] = req[conf.max] + 1;  
        calls.push(function(nextcb) {          
            httpRequest(otpl, type, back, itf.uuid,itf.next, req, res,
                ccutil.loadItpl(back[itf.uuid].itpl, itf.uuid, req), nextcb);
        });
    });

    async.parallel(calls, function(err, results) {
        var errMsg = null;
        if (err) {
            if(!results) { results = "unknown";}
            errMsg = "itf["+results+"] call err";
            logger.getLogger('html').error("call-err ％s [%s]", req.pathname, errMsg);
        }

        if(req[conf.cnt] >= req[conf.max]){
            ccutil.loadLast(type, errMsg, req, res);
        }
    });     
}

function httpPrepare(back, param, user) {
     var options = {hostname: back.host, port: back.port, path: back.iurl, method: back.meth,headers:null};
     options.headers={'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8','user':user};
     if(back.meth.toUpperCase() == 'GET' && back.type=='http'){
        options.path = back.iurl+"?"+qs.stringify(param);    
     }else if(back.type != 'http') {
        options.headers={'claz':((!param || !param.claz) ? '[]' : param.claz)};
     }

     return options;    
}

function httpRequest(otpl, type, back, uuid, next, req, res, param, callback){
    if(!param || (param.code != null && param.code != 0)) {
        req[conf.err][uuid] = true;
        req[conf.cnt] = req[conf.cnt] + 1;    
        callback(true, uuid + ':' + ((!param || !param.message) ? "unknown" : param.message));
        return;
    }

     var eventId=setTimeout(function(){
        reqObj.emit('timeout',ccutil.getError(-9008,"service timeout"));
     },conf.timeout);       

     var body = '';
     var option = httpPrepare(back[uuid], param, req.user);
     var reqObj = http.request(option, function(resObj){
         resObj.setEncoding('utf8');
         resObj.on('data',function(d){
             body += d;
         }).on('end', function(){
            if(!resObj.headers['err']) {
                try{
                    body = JSON.parse(body);//  eval('(' + body + ')');
                }catch(err){
                    body=ccutil.getError(-9009,"data-err "+resObj.statusCode);
                    if(back[uuid].type != 'http') {
                        body=ccutil.getError(-9009,
                            "["+resObj.statusCode+"]data-err:"+(resObj.headers.errs?resObj.headers.errs:'unknown'));
                    }
                    req[conf.err][uuid] = true;
                }

                req[conf.rst][uuid]=body;
                req[conf.cnt] = req[conf.cnt] + 1;
                if(!req[conf.err][uuid] && next && next.length > 0){
                    httpNext(otpl, type, back, next, req, res, uuid);
                }
            }else{
                req[conf.rst][uuid]=null;
                req[conf.err][uuid]=true;
                req[conf.cnt] = req[conf.cnt] + 1;
            }

            callback(null,null);
         }); 
     });    

     reqObj.on('timeout',function(err){
        reqObj.abort();
     }).on('error', function(err){
        req[conf.cnt] = req[conf.cnt] + 1;
        req[conf.err][uuid] = true;
        callback(err,uuid);
     });

     if(back[uuid].meth.toUpperCase() == 'POST' || back[uuid].type != 'http'){
        
        if(back[uuid].type != 'http') {
            reqObj.write(JSON.stringify((!param || !param.json) ? null : param.json));
        }else{
            reqObj.write(qs.stringify(param));
        }
     }   
         
     reqObj.end();
}

exports.tffile = function tffile(req,res, fields,files) {
    if(conf.ftp != null) {
        var array = [];
        for(var file in files){
            var original = '/'+Date.now()+'/'+ccutil.localIp()+'/'+files[file][0].originalFilename;
            files[file][0].originalFilename = ccutil.md5(original)+ccutil.fileExt(original);                  
            array.push(files[file][0]);
        }   

        array.forEach(function(file){
            var ftpClient = new ftp();  
            ftpClient.on('ready', function() {
                ftpClient.put(file.path, conf.ftp.path+file.originalFilename, function(err) {
                    if (err) {
                        logger.getLogger('file').error("file upload err by [%s]", file.path, JSON.stringify(err));                      
                    }
                    ftpClient.end();
                    ftpClient.destroy();

                    fs.exists(file.path, function (exists) {
                        if(exists){
                            logger.getLogger('file').error('ftp suc and delete file [%s]', file.path);
                            fs.unlink(file.path);
                        }
                    });                             
                });
            });         
            ftpClient.connect(conf.ftp);  
        }); 
    }
}

exports.httpRequest = httpRequest;
