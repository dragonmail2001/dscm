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
package org.cloudy.dscm.publisher;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cloudy.dscm.common.CConf;
import org.cloudy.dscm.common.CLogger;
import org.cloudy.dscm.context.CContextLoaderListener;

import org.cloudy.netty.bootstrap.ServerBootstrap;
import org.cloudy.netty.buffer.PooledByteBufAllocator;
import org.cloudy.netty.channel.Channel;
import org.cloudy.netty.channel.ChannelOption;
import org.cloudy.netty.channel.group.ChannelGroup;
import org.cloudy.netty.channel.group.DefaultChannelGroup;
import org.cloudy.netty.channel.nio.NioEventLoopGroup;
import org.cloudy.netty.channel.socket.nio.NioServerSocketChannel;
import org.cloudy.netty.util.concurrent.GlobalEventExecutor;
import org.cloudy.netty.util.internal.SystemPropertyUtil;
import org.cloudy.netty.util.internal.logging.InternalLoggerFactory;
//import org.cloudy.netty.util.internal.logging.Log4JLoggerFactory;

public class CServer {
	
	private static final int DEFAULT_THREADS;
    static {
    	DEFAULT_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "org.cloudy.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 4));
    }
    
	private CLogger logger;
	private int port = 5520;
	private int backlog = 10240;
	private int rcvbuf = 1024*256;
	private int sndbuf = 1024*256;
	private int aggregator = 1024*256;
	private boolean keepalive = true;
	private boolean debug = true;
	private boolean waste = true;
	private int threadSize = 0 ;
	private String heartbeat = null;

	private static CServer httpserver;
	private CContextLoaderListener loader;
	private ExecutorService threadPool = null;
	private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

//	public void addChannel(Channel channel) {
//		if(channels.find(channel.id()) == null) {
//			channels.add(channel);
//		}
//	}	
//	
//	public Channel find(Channel channel) {
//		return channels.find(channel.id());
//	}
	
	public boolean remove(Channel channel) {
		return channels.remove(channel);
	}
	
	public static CServer getInstance(CContextLoaderListener loader) {
		if(httpserver == null && loader != null) {
			httpserver = new CServer(loader);
		}
		return httpserver;
	}
	
	public static CLogger logger() {
		return getInstance().logger;
	}
	
	public static CServer getInstance() {
		return httpserver;
	}
	
	public CServer(CContextLoaderListener loader) {
		this.loader = loader;
	}
	
	public Object getBean(String name) throws Exception {
		if(name == null || name.length() <= 0) {
			throw new Exception("bean name is null");
		}
		return loader.classByName(name);
	}

	private ServerBootstrap bootstrap;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workGroup;
	
	public void stopHttpserver() {
		channels.close().awaitUninterruptibly();  
	}
	
	public boolean debug() {
		return debug;
	}
	
	public boolean waste() {
		return waste;
	}
	
	public int aggregator() {
		return aggregator;
	}
	
	public String heartbeat() {
		return heartbeat;
	}
	
	public ExecutorService threadPool() {
		return this.threadPool;
	}
	
	public void startHttpserver(Properties properties) throws Exception{
		//InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());
		String logname = properties.getProperty("logname");
		
		port = CConf.toInt(properties.getProperty("port"));
		backlog = CConf.toInt(properties.getProperty("backlog"));
		rcvbuf = CConf.toInt(properties.getProperty("rcvbuf"));
		sndbuf = CConf.toInt(properties.getProperty("sndbuf"));
		keepalive = CConf.toBool(properties.getProperty("keepalive"));
		debug = CConf.toBool(properties.getProperty("debug")); 
		waste = CConf.toBool(properties.getProperty("waste")); 
		threadSize = CConf.toInt(properties.getProperty("threadSize"), DEFAULT_THREADS);
		
		heartbeat = properties.getProperty("heartbeat");
		if(heartbeat == null || heartbeat.trim().length() <= 0) {
			heartbeat = "/heartbeat";
		}
		
		aggregator = CConf.toInt(properties.getProperty("aggregator")); 

		threadPool = Executors.newCachedThreadPool();//.newFixedThreadPool(threadSize);
		logger = new CLogger(InternalLoggerFactory.getInstance(logname));
		
		logger.log(new StringBuffer("threadPool=").append(threadSize));
		
		bootstrap = new ServerBootstrap();
		bossGroup = new NioEventLoopGroup();
		workGroup = new NioEventLoopGroup();
		
		bootstrap.group(bossGroup, workGroup);
		bootstrap.option(ChannelOption.SO_BACKLOG, backlog);
	    bootstrap.option(ChannelOption.SO_RCVBUF, rcvbuf);
	    bootstrap.option(ChannelOption.SO_SNDBUF, sndbuf);
	    bootstrap.option(ChannelOption.SO_LINGER, 0);
	    bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);  
	    bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, keepalive);	
	    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new CServerInitializer(this));
		
		logger.log(new StringBuilder().append("|backlog:").append(backlog).append("|rcvbuf:").append(rcvbuf).
				append("|sndbuf:").append(sndbuf).append("|keepalive:").append(keepalive).append("|port:").append(port).toString());

		final Channel channel;
		try {
			channel = bootstrap.bind(port).sync().channel();
		} catch (InterruptedException exc) {
			logger.log(exc.getMessage());
			throw exc;
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					channel.closeFuture().sync();
				} catch (InterruptedException exc) {
					exc.printStackTrace();
				} finally {
					workGroup.shutdownGracefully();
					workGroup.shutdownGracefully();
				}
			}
		}).start();
 
	}
}