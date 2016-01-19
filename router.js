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
var backer = require('./control/backer'); 
var cfform = require('./control/cfform');
var ccform = require('./control/ccform');
var ccleft = require('./control/ccleft');
var Router = require('express').Router;
var router = Router();

/**
 * 枚举所有请求路径，为了防止后续使用者不能清楚知道系统对外到底
 * 有哪些服务在运行（一个路径对应一个系统对外提供的服务入口）；
 *
 * 所有的对外服务请求路径都是用 filter.convert进行统一的数据
 * 适配
 *
 */
router.get('/*.htm', backer.convert);
router.post('/*.htm', backer.convert);

router.get('/*.do', ccleft.cookie, ccleft.ccleft, ccform.ccform);
router.post('/*.do', ccleft.cookie, ccleft.ccleft, ccform.ccform);

router.get('/*.ft', ccleft.cookie, ccleft.ccleft, cfform.cfform);
router.post('/*.ft', ccleft.cookie, ccleft.ccleft, cfform.cfform);

router.get('/*.vf', ccleft.verify);

module.exports = router;
