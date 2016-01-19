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

var logger = require('../logger.js');
var ccutil = require('../util/ccutil.js');
var conf = require('../configure');

function getParameter(req,name) {
    var value = req.query[name];
    if(value == null){
        value = req.body[name];
    }

    return value;
}

exports.convert = function(req, res) {
    var urlstr = req.url;
    var urlParsed = url.parse(urlstr, true);
    var pathname = urlParsed.pathname;
    if ("/manager.htm" != pathname) {
        res.jsonp(ccutil.getError(-9009, "未知请求"));
        return;
    }

    var opt = getParameter(req,'opt');
    if (typeof(opt) != "undefined" && opt =='del') {
        var uids = getParameter(req,'pa');
        
    } else {
        var context = getParameter(req,'path');
        var backurl = getParameter(req,'in_url');
        if(!context || !backurl) {
            result = ccutil.getError(-9023, "path or in_url null");
            res.jsonp(result);
            return;
        }

        var result = ccutil.getSuccess("0000", "add input template success");
        var filePath = path.join(conf.left, '/', ccutil.md5(context));

        console.log(filePath);
        fs.writeFile(filePath,backurl,{encoding:'utf8',mode:438,flag:'w+'},function(err){
            if (err) {
                logger.getLogger('filter').error('[%s worker %d failed], stack [%s] err[%s]', 
                    new Date(), process.pid, err.stack, err);
                result = ccutil.getError(-9023, "update input template failed");
            }
            res.jsonp(result);
        });
    }
};
