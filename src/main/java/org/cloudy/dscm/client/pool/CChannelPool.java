package org.cloudy.dscm.client.pool;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cloudy.dscm.client.handler.CChannelInitializer;
import org.cloudy.dscm.client.handler.CChannelPoolHandler;
import org.cloudy.netty.bootstrap.Bootstrap;
import org.cloudy.netty.channel.Channel;
import org.cloudy.netty.channel.ChannelInitializer;
import org.cloudy.netty.channel.ChannelOption;
import org.cloudy.netty.channel.EventLoopGroup;
import org.cloudy.netty.channel.group.ChannelGroup;
import org.cloudy.netty.channel.group.DefaultChannelGroup;
import org.cloudy.netty.channel.nio.NioEventLoopGroup;
import org.cloudy.netty.channel.socket.nio.NioSocketChannel;
import org.cloudy.netty.handler.codec.http.HttpClientCodec;
import org.cloudy.netty.handler.codec.http.HttpObjectAggregator;
import org.cloudy.netty.handler.logging.LogLevel;
import org.cloudy.netty.handler.logging.LoggingHandler;
import org.cloudy.netty.handler.timeout.IdleStateHandler;
import org.cloudy.netty.util.concurrent.GlobalEventExecutor;

import com.zhang.util.NettyHttpResponseFutureUtil;

public class CChannelPool {
    private static final Logger logger = Logger.getLogger(CChannelPool.class.getName());
	
	// channel pools per route
	private ConcurrentMap<String, LinkedBlockingQueue<Channel>> routeToPoolChannels;
	
	// max number of channels allow to be created per route
	private ConcurrentMap<String, Semaphore>                    maxPerRoute;
	
	// max time wait for a channel return from pool
	private int                                                 connectTimeOutInMilliSecondes;
	
	// max idle time for a channel before close
	private int                                                 maxIdleTimeInMilliSecondes;
	
	private CChannelInitializer                       			channelInitializer;
	
	/**
	* value is false indicates that when there is not any channel in pool and no new
	* channel allowed to be create based on maxPerRoute, a new channel will be forced
	* to create.Otherwise, a <code>TimeoutException</code> will be thrown
	* */
	private boolean                                             forbidForceConnect;
	
	// default max number of channels allow to be created per route
	private final static int                                    DEFAULT_MAX_PER_ROUTE = 200;
	
	private EventLoopGroup                                      group;
	
	private final Bootstrap                                     clientBootstrap;
	
	private static final String                                 COLON                 = ":";
	
    /**
     * Create a new instance of ChannelPool
     * 
     * @param maxPerRoute
     *            max number of channels per route allowed in pool
     * @param connectTimeOutInMilliSecondes
     *            max time wait for a channel return from pool
     * @param maxIdleTimeInMilliSecondes
     *            max idle time for a channel before close
     * @param forbidForceConnect
     *            value is false indicates that when there is not any channel in pool and no new
     *            channel allowed to be create based on maxPerRoute, a new channel will be forced
     *            to create.Otherwise, a <code>TimeoutException</code> will be thrown. The default
     *            value is false. 
     * @param additionalChannelInitializer
     *            user-defined initializer
     * @param options
     *            user-defined options
     * @param customGroup user defined {@link EventLoopGroup}
     */
    @SuppressWarnings("unchecked")
    public CChannelPool(Map<String, Integer> maxPerRoute, int connectTimeOutInMilliSecondes,
                            int maxIdleTimeInMilliSecondes, boolean forbidForceConnect,
                            CChannelInitializer channelInitializer,
                            Map<ChannelOption, Object> options, EventLoopGroup customGroup) {

        this.channelInitializer = channelInitializer;
        this.maxIdleTimeInMilliSecondes = maxIdleTimeInMilliSecondes;
        this.connectTimeOutInMilliSecondes = connectTimeOutInMilliSecondes;
        this.maxPerRoute = new ConcurrentHashMap<String, Semaphore>();
        this.routeToPoolChannels = new ConcurrentHashMap<String, LinkedBlockingQueue<Channel>>();
        this.group = null == customGroup ? new NioEventLoopGroup() : customGroup;
        this.forbidForceConnect = forbidForceConnect;

        this.clientBootstrap = new Bootstrap();
        clientBootstrap.group(group).channel(NioSocketChannel.class).option(
            ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast("log", new LoggingHandler(LogLevel.INFO));

                ch.pipeline().addLast(HttpClientCodec.class.getSimpleName(), new HttpClientCodec());
                if (null != CChannelPool.this.channelInitializer) {
                	CChannelPool.this.channelInitializer.initChannel(ch);
                }

                ch.pipeline().addLast(HttpObjectAggregator.class.getSimpleName(),
                    new HttpObjectAggregator(1048576));

                ch.pipeline().addLast(IdleStateHandler.class.getSimpleName(),
                    new IdleStateHandler(0, 0, CChannelPool.this.maxIdleTimeInMilliSecondes, TimeUnit.MILLISECONDS));

                ch.pipeline().addLast(CChannelPoolHandler.class.getSimpleName(),
                    new CChannelPoolHandler(CChannelPool.this));
            }

        });
        if (null != options) {
            for (Entry<ChannelOption, Object> entry : options.entrySet()) {
                clientBootstrap.option(entry.getKey(), entry.getValue());
            }
        }

        if (null != maxPerRoute) {
            for (Entry<String, Integer> entry : maxPerRoute.entrySet()) {
                this.maxPerRoute.put(entry.getKey(), new Semaphore(entry.getValue()));
            }
        }

    }
    
    /**
     * close all channels in the pool and shut down the eventLoopGroup
     * 
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        for (LinkedBlockingQueue<Channel> queue : routeToPoolChannels.values()) {
            for (Channel channel : queue) {
                removeChannel(channel, null);
                channelGroup.add(channel);
            }
        }
        channelGroup.close().sync();
        group.shutdownGracefully();
    }
    
    /**
     * remove the specified channel from the pool,cancel the responseFuture 
     * and release semaphore for the route
     * 
     * @param channel
     */
    private void removeChannel(Channel channel, Throwable cause) {

        InetSocketAddress route = (InetSocketAddress) channel.remoteAddress();
        String key = getKey(route);

        NettyHttpResponseFutureUtil.cancel(channel, cause);

        if (!NettyHttpResponseFutureUtil.getForceConnect(channel)) {
            LinkedBlockingQueue<Channel> poolChannels = routeToPoolChannels.get(key);
            if (poolChannels.remove(channel)) {
                logger.log(Level.INFO, channel + " removed");
            }
            getAllowCreatePerRoute(key).release();
        }
    } 
    
    private String getKey(InetSocketAddress route) {
        return route.getHostName() + COLON + route.getPort();
    }
    
    private Semaphore getAllowCreatePerRoute(String key) {
        Semaphore allowCreate = maxPerRoute.get(key);
        if (null == allowCreate) {
            Semaphore newAllowCreate = new Semaphore(DEFAULT_MAX_PER_ROUTE);
            allowCreate = maxPerRoute.putIfAbsent(key, newAllowCreate);
            if (null == allowCreate) {
                allowCreate = newAllowCreate;
            }
        }

        return allowCreate;
    } 
    
    private LinkedBlockingQueue<Channel> getPoolChannels(String route) {
        LinkedBlockingQueue<Channel> oldPoolChannels = routeToPoolChannels.get(route);
        if (null == oldPoolChannels) {
            LinkedBlockingQueue<Channel> newPoolChannels = new LinkedBlockingQueue<Channel>();
            oldPoolChannels = routeToPoolChannels.putIfAbsent(route, newPoolChannels);
            if (null == oldPoolChannels) {
                oldPoolChannels = newPoolChannels;
            }
        }
        return oldPoolChannels;
    }    
}
