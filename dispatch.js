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
var path = require('path');
var util = require('util');
var cfork = require('cfork');
var logger = require('./logger');
var config = require('./configure');

var workerPath = path.join(__dirname, 'worker.js');

if (config.enableCluster) {
  cfork({
    exec: workerPath,
    count: config.numCPUs
  }).on('fork', function (worker) {
    logger.getLogger('main').info('[%s] [worker:%d] new worker start', Date(), worker.process.pid);
  }).on('disconnect', function (worker) {
    logger.getLogger('main').error('[%s] [master:%s] wroker:%s disconnect, suicide: %s, state: %s.',
      Date(), process.pid, worker.process.pid, worker.suicide, worker.state);
  }).on('exit', function (worker, code, signal) {
    var exitCode = worker.process.exitCode;
    var err = new Error(util.format('worker %s died (code: %s, signal: %s, suicide: %s, state: %s)',
      worker.process.pid, exitCode, signal, worker.suicide, worker.state));
    err.name = 'WorkerDiedError';
    logger.getLogger('main').error('[%s] [master:%s] wroker exit: %s', Date(), process.pid, err.stack);
  });
} else {
  require(workerPath);
}

