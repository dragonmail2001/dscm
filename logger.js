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
var log4js = require('log4js');

/**
 * 
 * 定义两个日志输出，一个是main，另外一个filer，后续可根据应用需求
 * 任意增加日志输出，指定不同分类的日志输出到不同文件；
 *
 */
log4js.configure({
    appenders: [{
      category: 'main',
      type: 'file', 
      layout: {
        type: 'pattern',
        pattern: '%[%r (%x{pid}) %p %c -%] %m%n',
        tokens: {
            pid : function() { return process.pid; }
        }
      },				
      filename: config.ldir+'/main.log', 
      maxLogSize: 102400000,
      backups:3
    },{
      category: 'html',
      type: 'file', 
      layout: {
        type: 'pattern',
        pattern: '%[%r (%x{pid}) %p %c -%] %m%n',
        tokens: {
            pid : function() { return process.pid; }
        }
      },        
      filename: config.ldir+'/html.log', 
      maxLogSize: 102400000,
      backups:3
    },{
      category: 'file',
      type: 'file', 
      layout: {
        type: 'pattern',
        pattern: '%[%r (%x{pid}) %p %c -%] %m%n',
        tokens: {
            pid : function() { return process.pid; }
        }
      },        
      filename: config.ldir+'/file.log', 
      maxLogSize: 102400000,
      backups:3
    },{
      category: 'filter',
      type: 'file', 	
      layout: {
        type: 'pattern',
        pattern: '%[%r (%x{pid}) %p %c -%] %m%n',
        tokens: {
            pid : function() { return process.pid; }
        }
      },			
      filename: config.ldir+'/filter.log', 
      maxLogSize: 102400000,
      backups:3
    }],
    levels: {
      "[all]": config.loglevel  	
    }
});

/**
 * 
 * 外部引用日志时候的函数入口类
 * tmplog.getLogger('main').info('这是使用例子');
 *
 */
exports.getLogger=function(name){
  var logger = log4js.getLogger(name);
  return logger;
};


