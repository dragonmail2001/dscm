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
var url = require('url');
var path = require('path');
var util = require('util');
var async = require('async');
var multiparty = require('multiparty');

var conf = require('../configure');
var logger = require('../logger.js');
var ccutil = require('../util/ccutil.js');
var cchttp = require('../util/cchttp.js');

exports.cfform = function(req, res, next){
    var form = new multiparty.Form({uploadDir: conf.fdir+'/'});
    form.parse(req, function(errors, fields, files) {
	    var context = url.parse(req.url, true).pathname; 
	    var configure = ccutil.loadCfgConf(ccutil.parseUrl(context));    	
 		if(errors){
			logger.getLogger('file').error("file upload parase err by [%s]", JSON.stringify(errors));
			res.jsonp(ccutil.getError(-9301, JSON.stringify(errors)));
			return;
		} 

		cchttp.tffile(req, res, fields, files);

	    configure.mkey.forEach(function(mobj){
	        if(!req[conf.par][mobj['uuid']]){
	            req[conf.par][mobj['uuid']] = {};
	        }

	        var fkey = mobj['fkey'], bkey = mobj['bkey'], uuid = mobj['uuid'];

	        req[conf.par][uuid][bkey] = ccutil.getParameter(req,fkey);//fields[mobj['fkey']];
	        if(req[conf.par][uuid][bkey] == null) {
	        	req[conf.par][uuid][bkey] = fields[fkey];
	        	if(req[conf.par][uuid][bkey] == null && files[fkey].length > 0) {
	        		req[conf.par][uuid][bkey] = files[fkey][0].originalFilename;
	        	}	
	        }
	    });		

	    var calls = [], back = configure.back; configure.itfs.forEach(function(itf){
	        if(itf.dtpl != null) {
	            if(!ccutil.loadPara(itf.dtpl, itf.uuid, req)) {
	                return;
	            }          
	        }

			req[conf.max] = req[conf.max] + 1;
	        calls.push(function(formcb1) {                   
	            cchttp.httpRequest(configure.otpl, configure, back, itf.uuid, itf.next, req, res,
	                ccutil.loadItpl(back[itf.uuid].itpl, itf.uuid, req), formcb1);
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
    });
}