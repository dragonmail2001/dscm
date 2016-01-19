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
var http = require('http');
var path = require('path');

var express = require('express');
var bodyParser = require('body-parser'); 
var cookieParser = require('cookie-parser');
var session = require('express-session');
var redisStore = require('connect-redis')(session);
var router = require('./router');
var conf = require('./configure');

var app = express();
app.use(cookieParser('sdcm keyboard'));
app.use(session({
	store: new redisStore({
    	host: conf.rdip,
    	port: conf.rdpt,
    	db: conf.rdid
  	}),
  	resave:false,
  	saveUninitialized:true,
  	secret: conf.rdky
}));
//app.use(express.session());
//app.set('view engine', 'html');
//app.engine('html', require('ejs-mate'));
//app.set('views', __dirname + '/template');
//app.set('views', conf.left);
//app.set('view engine', 'ejs');

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use('/', router);
app.use(express.static(path.join(__dirname, 'web')));

module.exports = http.createServer(app);

