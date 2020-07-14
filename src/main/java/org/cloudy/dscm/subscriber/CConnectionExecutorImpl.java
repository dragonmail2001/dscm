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
package org.cloudy.dscm.subscriber;

import org.cloudy.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.CodingErrorAction;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.cloudy.dscm.common.CConf;
import org.cloudy.dscm.common.CParameter;
import org.cloudy.dscm.common.CLogger;
import org.cloudy.dscm.publisher.CServer;
import org.cloudy.fastjson.JSON;
import org.cloudy.fastjson.JSONException;

public class CConnectionExecutorImpl implements CConnectionExecutor {
	private PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	public static final CConnectionExecutor INST = new CConnectionExecutorImpl();
	private RequestConfig requestConfig = null;
	
	private static class IdleConnectionMonitor extends Thread {
		private final HttpClientConnectionManager httpClientConnectionManager;
	    private long checkTime; 
	    private long closeTime;
	    
	    public IdleConnectionMonitor(HttpClientConnectionManager httpClientConnectionManager, long checkTime, long closeTime) {
	        super();
	        
	        this.httpClientConnectionManager = httpClientConnectionManager;
	        this.checkTime = checkTime;
	        this.closeTime = closeTime;
	    }
	    
	    @Override
	    public void run() {
	        try {
	            while (true) {
	                synchronized (this) {
	                    wait(checkTime);
	                    // Close expired connections
	                    httpClientConnectionManager.closeExpiredConnections();
	                    // Optionally, close connections
	                    // that have been idle longer than 30 sec
	                    httpClientConnectionManager.closeIdleConnections(closeTime, TimeUnit.SECONDS);
	                }
	            }
	        } catch (InterruptedException exc) {
	        	CServer.logger().log(new StringBuilder("CSubscriber pool error!!").append(exc.getMessage()));
	        }
	    }
	}

	
	private CConnectionExecutorImpl() {
		setConfigure();
		
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true).setSoKeepAlive(false)
                .setSoLinger(0).build();
		connectionManager.setDefaultSocketConfig(socketConfig);
		
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(CConf.CUTF)
                .setMessageConstraints(MessageConstraints.custom().build())
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);	

        new IdleConnectionMonitor(connectionManager, checkTime, closeTime).start();
        
	}
	
	private CLogger logger;
	private boolean debug;
	private boolean waste;
	private String asyHost;
	private long checkTime, closeTime, asySize;
	private void setConfigure() {
		try {
			InputStream is = CConnectionExecutor.class.getClassLoader().
					getResourceAsStream("dscm.properties");
			
			Properties properties=new Properties();
			properties.load(is);
			is.close();
			
//			int capaticy = CConf.toInt(properties.getProperty("capaticy"));
//			long delay = CConf.toLong(properties.getProperty("delay"));
//			//int fmax = CConf.toInt(properties.getProperty("fmax"));
//			int lmax = CConf.toInt(properties.getProperty("lmax"));
//			int pool = CConf.toInt(properties.getProperty("pool"));
			waste = CConf.toBool(properties.getProperty("waste"));
			debug = CConf.toBool(properties.getProperty("debug")); 
			asyHost = properties.getProperty("asyHost"); 
			asySize = CConf.toLong(properties.getProperty("asySize"));
//			String path = properties.getProperty("path");
			String logname = properties.getProperty("logname");
			
			int maxTimeout = CConf.toInt(properties.getProperty("maxTimeout"));
			int maxTotal = CConf.toInt(properties.getProperty("maxTotal"));
			int defaultMaxPerRoute = CConf.toInt(properties.getProperty("defaultMaxPerRoute"));
			
			this.checkTime = CConf.toLong(properties.getProperty("checkTime"));
			this.closeTime = CConf.toLong(properties.getProperty("closeTime"));
			
			this.connectionManager.setMaxTotal(maxTotal);
			this.connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
			
			
			this.requestConfig = RequestConfig.custom().setSocketTimeout(maxTimeout).setConnectTimeout(maxTimeout)
					.setConnectionRequestTimeout(maxTimeout).setStaleConnectionCheckEnabled(true).build();
			
			logger = new CLogger(InternalLoggerFactory.getInstance(logname));
		} catch(Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private CloseableHttpClient getHttpClient() {
		return HttpClients.custom().setConnectionManager(connectionManager).build();
	}	
	
	private CParameter post(CloseableHttpClient httpClient, String url, Method method, String claz, String json, String sync, String safe, String uuid) throws Throwable {
		String content = null;
		CParameter result = new CParameter();
		CloseableHttpResponse response = null;
		String auth = new URL(url).getAuthority();

		url = new StringBuilder(url).append("?").append(CConf.ACTN).append("=").append(method.getName()).
				append("&").append(CConf.UUID).append("=").append(uuid).toString();
		
		if(!CConf.SYNC.equalsIgnoreCase(sync)) {
			if(method.getReturnType() != Boolean.class && method.getReturnType() != boolean.class) {
				throw new RuntimeException("async call function must return boolean or Boolean");
			}
			
			if(json.length() > asySize) {
				throw new RuntimeException("async message size greater asySize @see dscm-conf");
			}
			
			claz = "['java.lang.String','java.lang.String','java.lang.String','java.lang.String','java.lang.String','java.lang.String']";
			String[] array = new String[]{UUID.randomUUID().toString(), url, claz, json, safe, auth};
			json = JSON.toJSONString(array,CConf.FEATURE);
			
			url = new StringBuilder(asyHost).append("/asycService?actn=create").toString();
			sync = CConf.SYNC;
		}
		
		HttpPost httpPost = new HttpPost(url);
		
		long currentTimestamp = System.currentTimeMillis();
		try {
			if(debug) {
				logger.log(new StringBuffer(httpPost.getURI().getQuery()).append(" ").
						append(sync).append(" ").append(claz).append(" ").
						append(json));
			}
			
			httpPost.setHeader(CConf.CLAZ, claz);
			httpPost.setHeader(CConf.SYNC, sync);
			httpPost.setEntity(new StringEntity(json, CConf.UTF8));
			
			httpPost.setConfig(requestConfig);
			response = httpClient.execute(httpPost);
		
			HttpEntity httpEntity = response.getEntity();
			content = EntityUtils.toString(httpEntity, CConf.UTF8);
		} catch(Exception exc) {
			throw new RuntimeException(new StringBuffer(url).
					append(exc.getMessage()).toString(), exc.getCause());
		} finally {
			if(response != null) {
				try {
					response.close();
				} catch (IOException exc) {
					throw new RuntimeException(exc.getCause());
				}
			}
			
			CConnectionExecutorImpl.INST.waste(new StringBuffer(url).append(" http-client-time=").
					append(System.currentTimeMillis()-currentTimestamp));
		}
			
		Header[] errs = response.getHeaders("errs");
		Header[] type = response.getHeaders("claz");
		
		if(debug) {
			logger.log(new StringBuffer(httpPost.getURI().getQuery()).append(" ").
					append(errs).append(" ").append(type).append(" ").
					append(content));
		}
		
		if(type.length < 1) {
			throw new RuntimeException("dscm-claz-err");
		}

		Object object = null;
		try {
			Class<?> ctarg = CParameter.parse(type[0].getValue());
			object = JSON.parseObject(content, ctarg);
		}catch(JSONException exc) {
			throw new RuntimeException(new StringBuffer(type[0].getValue()).
					append(" ").append(exc.getMessage()).append(content).toString());
		}catch(Exception exc) {
			throw new RuntimeException(exc.getCause() != null ? exc.getCause() : exc);
		}
		
		if(errs.length > 0) {
			if(object instanceof Throwable) {
				throw (Throwable)object;
			}else{
				throw new RuntimeException(object.toString());
			}
		}

		result.object(object);

		return result;
	}

	public Object execute(Object proxy, Method method, Object[] args, String url, String sync, String safe, String uuid) throws Throwable {
		String clazz = null, cjson = null;
	    CloseableHttpClient httpClient = getHttpClient();
		try {
			clazz = JSON.toJSONString(method.getParameterTypes(),CConf.FEATURE);
			cjson = JSON.toJSONString(args,CConf.FEATURE);
		}catch(Throwable exc) {
			throw exc;
		}
		
		return post(httpClient, url, method, clazz, cjson, sync, safe, uuid).object();
	}
	
	public void waste(StringBuffer sb) {
		if(waste) {
			logger.log(sb);
		}
	}
}
	
	

