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
var os = require('os');   
var fs = require('fs'); 
var ejs = require('ejs'); 
var path = require('path');
var crypto = require('crypto');
var buffer = require("buffer");
var conf = require('../configure');
var logger = require('../logger.js');

function renderHtml(configure, req, res) {
    var name = path.join(conf.back, '/', parseUrl(configure.otpl));
    var data = {
        "err":req[conf.err],
        "usr":req.user, 
        "par":req[conf.par], 
        "rst":req[conf.rst],
        "env":conf.env
    };
    
    var html = null;
    try {
        if(conf.debug){
            html = ejs.render(fs.readFileSync(name, 'utf8'), {data:data}, {cache:false,filename: name}); 
        }else{
            html = ejs.render(fs.readFileSync(name, 'utf8'), {data:data}, {cache:true,filename: name}); 
        }

        if(data.usr && data.usr.user) {
            req.session.user = data.usr.user;
        }

        res.writeHead(200, {'Content-Type': 'text/html;encode=UTF-8'});
    }catch(exc) {
        res.writeHead(500, {'Content-Type': 'text/html;encode=UTF-8'});
        logger.getLogger('html').error("load-mod-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack); 
        html = "name: " + exc.name + 
            "message: " + exc.message + 
            "lineNumber: " + exc.lineNumber + 
            "fileName: " + exc.fileName + 
            "stack: " + exc.stack;
        if(conf.debug) {
            res.write('err:'+JSON.stringify(configure,null,4));
            res.write('<br/><br/>');            
            res.write('err:'+JSON.stringify(data,null,4));
            res.write('<br/><br/>');
        }    
    }

    res.end(html); 
}

exports.getClientIp = function getClientIp(req) {
    return req.headers['x-forwarded-for'] ||
    req.connection.remoteAddress ||
    req.socket.remoteAddress ||
    req.connection.socket.remoteAddress;
}

exports.getParameter = function getParameter(req,name) {
    var value = req.query[name];
    if(value == null){
        value = req.body[name];
    }

    return value;
}

function getError(code,message) {
    return {
            "code": code,
            "success": false,
            "message": message
       };
}

exports.getError = getError;

function md5(data) {
    var buf = new buffer.Buffer(data);
    var str = buf.toString('binary');
    return crypto.createHash('md5').update(str).digest('hex');
}

exports.md5 = md5;

function cleftUrl(data) {
    if(!data || data.length <= 0) {
        return [data,0];
    }

    var str = [], pos = 0, cur = 0, cnt = 0;
    for (var i=0;i<data.length;i++){
        var c = data.charAt(i);
        if(c == '.' || c == '/' || c == ':') {
            if(c == '/' && (cnt++) <= 2){
                pos = cur;
            }
            continue;
        } 

        cur++;
        str.push(c);
    } 

    var curl = str.join('');
    return [curl,curl.substr(0,pos)];
}

function parseUrl(data) {
    var curl = cleftUrl(data);
    return curl[0];
}

exports.parseUrl = parseUrl;
exports.cleftUrl = cleftUrl;

exports.loadModule = function loadModule(module, req) {
    var result = null;
    try {
        var pathname = path.join(conf.back, '/', md5(module));
        if(conf.debug){
            delete require.cache[require.resolve(pathname)];
        }
        result = require(pathname)(req);
    } catch (exc) {
        logger.getLogger('html').error("load-mod-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);        
        result=null;
    }  
    return result;
}

exports.loadItpl = function loadItpl(module, uuid, req) {
    var result = null;
    try {
        var pathname = path.join(conf.back, '/', parseUrl(module));
        if(conf.debug){
            delete require.cache[require.resolve(pathname)];
        }
        result = require(pathname)(conf.env, req[conf.err],uuid, req.user, 
            req[conf.par][uuid], req[conf.rst]);
    } catch (exc) {
        logger.getLogger('html').error("load-itpl-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);        
        result=null;
    }  
    return result;
}

exports.loadPara = function loadPara(module, uuid, req) {
    var result = null;
    try {
        var pathname = path.join(conf.back, '/', parseUrl(module));
        if(conf.debug){
            delete require.cache[require.resolve(pathname)];
        }
        result = require(pathname)(conf.env, req[conf.err],uuid, req.user, 
                        req[conf.par], req[conf.rst]);
    } catch (exc) {
        logger.getLogger('html').error("load-para-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);
        result=null;
    }  
    req[conf.par][uuid] = result;
    return result;
}

exports.loadUrlConf = function loadUrlConf(module) {
    var result = null;
    try {
        var pathname = path.join(conf.left, '/', module);
        if(conf.debug){
            delete require.cache[require.resolve(pathname)];
        }
        result = require(pathname)(conf.env);
    } catch (exc) {
        logger.getLogger('html').error("load-url-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);        
        result=null;
    }
    return result;
}

exports.loadCfgConf = function loadUrlConf(module) {
    var result = null;
    try {
        var pathname = path.join(conf.cdir, '/', module);
        if(conf.debug){
            delete require.cache[require.resolve(pathname)];
        }
        result = require(pathname)(conf.env);
    } catch (exc) {
        logger.getLogger('html').error("load-url-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);        
        result=null;
    }
    return result;
}

exports.loadLast = function loadLast(configure, err, req, res) {
    if(!configure){
        res.jsonp(getError(-9000,"configure-err"));
        return;
    }

    var pathname = path.join(conf.back, '/', parseUrl(configure.otpl));
    if(!configure || configure.type != 'json') {
        renderHtml(configure, req, res);
    }else{
        var result = null;
        try {
            if(conf.debug){
                delete require.cache[require.resolve(pathname)];
            }
            result = require(pathname)(conf.env, err, req[conf.err], req.user, 
                req[conf.par], req[conf.rst]);
        } catch (exc) {
            logger.getLogger('html').error("load-last-err [％s] [％s] [％s] [％s] [%s]", 
            exc.name, exc.message, exc.lineNumber, exc.fileName, exc.stack);
            result=getError(-9000,"load-tpl-err");
        }  
        res.jsonp(result);
    }
}

exports.fileExt = function fileExt(data) {
    if(data == null){
        return null;
    }

    var pos = data.lastIndexOf('.');
    if(pos < 0){
        return '';
    }
    return data.substr(pos);
}

exports.localIp = function localIp() {
    var eth0 = os.networkInterfaces().eth0;
    if(eth0 == null) {
        eth0 = os.networkInterfaces().en0;
    }
    
    for(var i=0;i<eth0.length;i++){  
        if(eth0[i].family=='IPv4'){  
            return eth0[i].address;  
        }  
    } 
    return Date.now();
}