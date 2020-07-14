package org.cloudy.dscm.util;

import org.cloudy.dscm.common.CConf;
import org.cloudy.fastjson.JSON;

public class Generator {
	private Generator(){}
	
	public static String parameterType2String(Class<?> cid, String method) {
		StringBuffer sb = new StringBuffer();
		java.lang.reflect.Method[] mobjs = cid.getMethods();
		for(java.lang.reflect.Method mobj : mobjs) {
			if(mobj.getName().equals(method)) {
				sb.append(" ");
				sb.append(JSON.toJSONString(mobj.getParameterTypes(),CConf.FEATURE));
			}
		}
		return sb.toString();
	}
	
	public static String parameterValue2String(Object[] args) {
		return JSON.toJSONString(args,CConf.FEATURE);
	}	
}
