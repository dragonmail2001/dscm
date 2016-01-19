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

var config = require('./configure');
var graceful = require('graceful');
var logger = require('./logger');
var app = require('./app');

app.listen(config.httpport);
console.log('[%s] [worker:%d] Server started, listen at %d', new Date(), process.pid, config.httpport);
logger.getLogger('main').info('[%s] [worker:%d] Server started, listen at %d', new Date(), process.pid, config.httpport);

graceful({
    server: [app],
    error: function (err, throwErrorCount) {
        if (err.message) {
            err.message += ' (uncaughtException throw ' + throwErrorCount + ' times on pid:' + process.pid + ')';
        }

        console.error("[%s] [worker:%d] stack [%s] err[%s]"+err.stack,new Date(), process.pid, err.stack, err);
		logger.getLogger('main').error('[%s worker %d failed], stack [%s] err[%s]', new Date(), process.pid, err.stack, err);
    }
});
