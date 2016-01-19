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
var url = require('url');
var path = require('path');
var util = require('util');
var async = require('async');
var rp = require('request-promise');
var ccap = require('ccap');

var conf = require('../configure');
var logger = require('../logger.js');
var ccutil = require('../util/ccutil.js');

exports.cookie = function cookie(req, res, next) {
    req[conf.cnt] = 0, req[conf.max] = 0, req[conf.err] = {};
    req[conf.rst] = {}, req[conf.par] = {}; req.user = {};

    req.user["code"] = req.session.code;
    req.user["user"] = req.session.user;
    req.user["addr"] = ccutil.getClientIp(req);    
    res.cookie(conf.ckey,req.sessionID,conf.cset);

    var context = url.parse(req.url, true).pathname; 
    var name = ccutil.cleftUrl(context);
    var host = ccutil.loadUrlConf('gconfjs');

    if(!host || !host[name[1]]){
        var err = "itf[gconfjs] err";
        res.jsonp(ccutil.getError(-8000, err));
        return ;
    } 

    var pathname = path.join(conf.cdir, '/', name[0]);
    var itpl = host[name[1]] + name[0];
    var ciurl = []; ciurl.push(function(leftcb0){
        fs.exists(pathname, function(have) {
            leftcb0(null, {'itpl':itpl,'path':pathname,'have':have});
        });
    });

    async.parallel(ciurl, function(err, results) {
        var curl = results[0]; if(conf.debug || !curl.have) {
            rp(curl.itpl).then(function (body) {
                fs.writeFileSync(curl.path, body,{encoding:'utf8',mode:438,flag:'w+'});
                next(); 
            }).catch(function (err) {
                res.jsonp(ccutil.getError(-8001, err));
            });           
        } else {
            next();
        }
    }); 
}

exports.verify = function verify(req, res, next) {
    var w = ccutil.getParameter(req,'w');
    var h = ccutil.getParameter(req,'h');
    var o = ccutil.getParameter(req,'o');
    var f = ccutil.getParameter(req,'f');
    var q = ccutil.getParameter(req,'q');

    if(!w || w > 256 || w <= 0) {
        w = 256;
    }

    if(!h || h > 60 || h <= 0) {
        h = 60;
    }   

    if(!o || o > 40 || o <= 0) {
        o = 40;
    }

    if(!q || q > 100 || q <= 0) {
        q = 50;
    }

    if(!f || f > 57 || f <= 0) {
        f = 57;
    }

    var imvf = ccap({width:w,height:h,offset:o,quality:q,fontsize:f,
        generate:function(){
            var rstr='', cstr = ['0','1','2','3','4','5','6','7','8','9'];
            for(var i = 0; i < 4 ; i ++) {
                 var id = Math.ceil(Math.random()*10)%10;
                 rstr += cstr[id];
             }
            return rstr;
        }
    });
            
    var array = imvf.get();
    req.session.code = array[0];
    res.end(array[1]);
}

exports.ccleft = function(req, res, next) {
    var context = url.parse(req.url, true).pathname; 
    var configure = ccutil.loadCfgConf(ccutil.parseUrl(context));
    if(!configure || (configure.auth && !req.session.user)){
        var err = "itf[" + context + "] auth err";
        res.jsonp(ccutil.getError(-8003, err));
        return ;
    }   

    configure.mkey.forEach(function(mobj){
        if(!req[conf.par][mobj['uuid']]){
            req[conf.par][mobj['uuid']] = {};
        }
        req[conf.par][mobj['uuid']][mobj['bkey']] = ccutil.getParameter(req,mobj['fkey']);
    });

    var ciurl = []; configure.urls.forEach(function(curl){
        var pathname = path.join(conf.back, '/', ccutil.parseUrl(curl.name));
        if(curl.ext != null) {
            pathname = pathname + curl.ext;
        }
        ciurl.push(function(leftcb2){
            fs.exists(pathname, function(have) {
                leftcb2(null, {'itpl':curl.itpl,'path':pathname,'have':have});
            });
        });
    });

    async.parallel(ciurl, function(err, results) {
        var curls=[];results.forEach(function(curl){
            if(conf.debug || !curl.have) {
                curls.push(function(leftcb3){
                    rp(curl.itpl).then(function (body) {
                        fs.writeFileSync(curl.path, body,{encoding:'utf8',mode:438,flag:'w+'});
                        leftcb3(null,true);
                    }).catch(function (err) {
                        leftcb3(err,false);
                    });  
                });
            }
        });

        async.parallel(curls, function(err, results) {
            if(err) {
                logger.getLogger('html').error("tpl-err ％s [%s]", req.pathname, 
                    JSON.stringify(err));
                res.jsonp(ccutil.getError(-8004, "itf["+context+"] prepare err"));
                return;
            }
            next();
        });

    });
};