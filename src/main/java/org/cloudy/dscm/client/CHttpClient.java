package org.cloudy.dscm.client;

//import java.net.InetSocketAddress;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.cloudy.dscm.client.handler.CChannelInitializer;
//import org.cloudy.dscm.client.pool.CChannelPool;
//import org.cloudy.netty.bootstrap.Bootstrap;
//import org.cloudy.netty.channel.ChannelOption;
//import org.cloudy.netty.channel.EventLoopGroup;
//import org.cloudy.netty.channel.nio.NioEventLoopGroup;
//import org.cloudy.netty.channel.pool.ChannelPoolHandler;
//import org.cloudy.netty.channel.pool.FixedChannelPool;
//import org.cloudy.netty.channel.socket.nio.NioSocketChannel;
//import org.cloudy.netty.handler.codec.http.HttpMethod;
//import org.cloudy.netty.handler.codec.http.HttpRequest;

public class CHttpClient {
//	public static final class CConfigBuilder {
//		@SuppressWarnings({ "unchecked", "rawtypes" })
//		private Map<ChannelOption, Object> options = new HashMap<ChannelOption, Object>();
//
//		// max idle time for a channel before close
//		private int maxIdleTimeInMilliSecondes;
//
//		// max time wait for a channel return from pool
//		private int connectTimeOutInMilliSecondes;
//
//		/**
//		 * value is false indicates that when there is not any channel in pool
//		 * and no new channel allowed to be create based on maxPerRoute, a new
//		 * channel will be forced to create.Otherwise, a
//		 * <code>TimeoutException</code> will be thrown value is false.
//		 */
//		private boolean forbidForceConnect = false;
//
//		private CChannelInitializer channelInitializer;
//
//		// max number of channels allow to be created per route
//		private Map<String, Integer> maxPerRoute;
//
//		private EventLoopGroup customGroup;
//
//		public CConfigBuilder() {
//		}
//
//		public CHttpClient build() {
//			return new CHttpClient(this);
//		}
//
//		public CConfigBuilder maxPerRoute(Map<String, Integer> maxPerRoute) {
//			this.maxPerRoute = maxPerRoute;
//			return this;
//		}
//
//		public CConfigBuilder connectTimeOutInMilliSecondes(
//				int connectTimeOutInMilliSecondes) {
//			this.connectTimeOutInMilliSecondes = connectTimeOutInMilliSecondes;
//			return this;
//		}
//
//		@SuppressWarnings("unchecked")
//		public CConfigBuilder option(@SuppressWarnings("rawtypes") ChannelOption key, Object value) {
//			options.put(key, value);
//			return this;
//		}
//
//		public CConfigBuilder maxIdleTimeInMilliSecondes(
//				int maxIdleTimeInMilliSecondes) {
//			this.maxIdleTimeInMilliSecondes = maxIdleTimeInMilliSecondes;
//			return this;
//		}
//
//		public CConfigBuilder channelInitializer(CChannelInitializer channelInitializer) {
//			this.channelInitializer = channelInitializer;
//			return this;
//		}
//
//		public CConfigBuilder customGroup(EventLoopGroup customGroup) {
//			this.customGroup = customGroup;
//			return this;
//		}
//
//		public CConfigBuilder forbidForceConnect(boolean forbidForceConnect) {
//			this.forbidForceConnect = forbidForceConnect;
//			return this;
//		}
//
//		@SuppressWarnings({ "unchecked", "rawtypes" })
//		public Map<ChannelOption, Object> getOptions() {
//			return options;
//		}
//
//		public int getMaxIdleTimeInMilliSecondes() {
//			return maxIdleTimeInMilliSecondes;
//		}
//
//		public CChannelInitializer getChannelInitializer() {
//			return channelInitializer;
//		}
//
//		public Map<String, Integer> getMaxPerRoute() {
//			return maxPerRoute;
//		}
//
//		public int getConnectTimeOutInMilliSecondes() {
//			return connectTimeOutInMilliSecondes;
//		}
//
//		public EventLoopGroup getGroup() {
//			return this.customGroup;
//		}
//
//		public boolean getForbidForceConnect() {
//			return this.forbidForceConnect;
//		}
//	}
//	
//    private CChannelPool channelPool;
//
//    private CConfigBuilder    configBuilder;
//
//    private CHttpClient(CConfigBuilder configBuilder) {
//        this.configBuilder = configBuilder;
//        this.channelPool = new CChannelPool(configBuilder.getMaxPerRoute(), configBuilder
//            .getConnectTimeOutInMilliSecondes(), configBuilder.getMaxIdleTimeInMilliSecondes(),
//            configBuilder.getForbidForceConnect(), configBuilder.getChannelInitializer(),
//            configBuilder.getOptions(), configBuilder.getGroup());
//    }
//
//    public NettyHttpResponseFuture doPost(NettyHttpRequest request) throws Exception {
//
//        HttpRequest httpRequest = NettyHttpRequestUtil.create(request, HttpMethod.POST);
//        InetSocketAddress route = new InetSocketAddress(request.getUri().getHost(), request
//            .getUri().getPort());
//
//        return channelPool.sendRequest(route, httpRequest);
//    }
//
//    public NettyHttpResponseFuture doGet(NettyHttpRequest request) throws Exception {
//        HttpRequest httpRequest = NettyHttpRequestUtil.create(request, HttpMethod.GET);
//        InetSocketAddress route = new InetSocketAddress(request.getUri().getHost(), request
//            .getUri().getPort());
//        return channelPool.sendRequest(route, httpRequest);
//    }
//
//    public void close() throws InterruptedException {
//        channelPool.close();
//    }
//
//    public CConfigBuilder getConfigBuilder() {
//        return configBuilder;
//    }
//
//    public void setConfigBuilder(CConfigBuilder configBuilder) {
//        this.configBuilder = configBuilder;
//    }
//	
//	public static void main(String[] arg) {
//		Bootstrap bootstrap = new Bootstrap().channel(NioSocketChannel.class).group(
//				new NioEventLoopGroup());
//
//				//自定义的channelpoolhandler
//				ChannelPoolHandler handler = new ChannelPoolHandler();
//
//				//创建一个FixedChannelPool
//				FixedChannelPool pool = new FixedChannelPool(bootstrap, handler, 1000);
//	}
}
