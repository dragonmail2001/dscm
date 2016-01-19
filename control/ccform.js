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

var conf = require('../configure');
var logger = require('../logger.js');
var ccutil = require('../util/ccutil.js');
var cchttp = require('../util/cchttp.js');


exports.ccform = function(req, res) {   
    var context = url.parse(req.url, true).pathname; 
    var configure = ccutil.loadCfgConf(ccutil.parseUrl(context));
    var calls = [],back = configure.back; configure.itfs.forEach(function(itf){
        if(itf.dtpl != null) {
            if(!ccutil.loadPara(itf.dtpl, itf.uuid, req)) {
                return;
            }          
        }

        req[conf.max]=req[conf.max]+1; 
        calls.push(function(formcb0) {              
            cchttp.httpRequest(configure.otpl, configure, back, itf.uuid, itf.next, req, res,
                ccutil.loadItpl(back[itf.uuid].itpl, itf.uuid, req), formcb0);
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
            ccutil.loadLast(configure, errMsg, req, res);
        }
    });    
};
