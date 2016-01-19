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

var config  = {
    loglevel: 'DEBUG',
    rst: '___rst___',
    cnt: '___cnt___',
    max: '___max___',    
    err: '___err___',
    par: '___par___',
    env: 'dev',
    ftp:{
	    host: "192.168.18.230",
	    port:21,
	    user: "u1",
	    password: "123456",
	    keepalive:10000,
	    path:""
    },    
    cset: { maxAge: 20000,httpOnly:true, path:'/'},
    ckey: 'aha',
    wdir: path.join(__dirname, '../'),
    left: '/Workspace/002-ZC-NODEJS/04-Code/left',
    back: '/Workspace/002-ZC-NODEJS/04-Code/back',
    fdir: '/Workspace/002-ZC-NODEJS/04-Code/fdir',
    ldir: '/Workspace/002-ZC-NODEJS/04-Code/ldir',
    cdir: '/Workspace/002-ZC-NODEJS/04-Code/cdir',
    rdip: '192.168.165.128',
    rdpt: '6379',
    rdid: 1,
    rdky: 'sdcm keyboard'
};

module.exports = config;