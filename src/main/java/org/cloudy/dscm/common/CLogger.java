package org.cloudy.dscm.common;

import org.cloudy.netty.util.internal.logging.InternalLogger;

public class CLogger {    
//	private String date() {
//		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
//		return format.format(new Date());
//	}
//	
//	private String dirs() {
//		return new StringBuilder(dirs).append(File.separator).append(pkey).
//				append(date()).append(".log").toString();
//	}
	
	
	private InternalLogger logger = null;
	
    public void log(String str) {
    	logger.info(str);
    }
    
    public void log(StringBuilder str) {
    	logger.info(str.toString());
    }
    
    public void log(StringBuffer str) {
    	logger.info(str.toString());
    }

    /**
     * 
     * @param capaticy  缓存大小
     * @param delay     timer执行时间
     * @param fmax	           日志文件最大字节数
     * @param lmax	 	 内存最大缓存字节数
     * @param pool	            线程池最大数量
     * @param path      日志文件路径
     * @throws IOException
     */
	public CLogger(InternalLogger logger) {
		this.logger = logger;
	}
}
