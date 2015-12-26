package org.xzc.bilibili.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.RequestBuilder;
import org.xzc.http.Params;

public class Sign {
	public static final String appkey = "c1b107428d337928";
	//public static final String appkey = "84b739484c36d653";
	public static final String se = "ea85624dfcf12d7cc7b2b3a94fac1f2c";
	//public static final String se = "92209092eed6a4030308b38964f9c7fa";

	public Sign() {
	}

	public Sign(Params params) {
		List<NameValuePair> paramList = params.getParamList();
		Map<String, String> map = new HashMap<String, String>();
		for (NameValuePair nvp : paramList) {
			map.put( nvp.getName(), nvp.getValue() );
		}
		parse( map );
	}

	public Sign(Object... args) {
		Map<String, String> params = new HashMap<String, String>();
		for (int i = 0; i < args.length; i += 2) {
			String name = args[i].toString();
			String value = args[i + 1].toString();
			params.put( name, value );
		}
		parse( params );
	}

	public String getAppkey() {
		return appkey;
	}

	public Sign(String query) {
		Map<String, String> params = new HashMap<String, String>();
		String[] ss = query.split( "&" );
		for (String s : ss) {
			String[] ss2 = s.split( "=" );
			params.put( ss2[0], ss2[1] );
		}
		parse( params );
	}

	private void parse(Map<String, String> params) {
		List<String> keys = new ArrayList<String>( params.keySet() );
		Collections.sort( keys );
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		boolean first = true;
		for (String key : keys) {
			//(key.equals( "appkey" ))continue;
			//(key.equals( "platform" ))continue;
			//f(key.equals( "_device" ))continue;
			//if(key.equals( "build" ))continue;
			//			if(key.equals( "access_key" ))continue;
			String value = params.get( key );
			if (!first)
				sb.append( '&' );
			first = false;
			sb.append( key );
			sb.append( '=' );
			try {
				sb.append( URLEncoder.encode( value, "utf-8" ) );
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		String s = sb.toString();
		sign = DigestUtils.md5Hex( s + se );
		result = s + "&sign=" + sign;
	}

	public Sign(Map<String, String> params) {
		parse( params );
	}

	private String result;
	private String sign;

	public String getResult() {
		return result;
	}

	public String getSign() {
		return sign;

	}

	public static void signTo(RequestBuilder rb) {
		List<NameValuePair> parameters = rb.getParameters();
		Map<String, String> params = new HashMap<String, String>();
		for (NameValuePair nvp : parameters)
			params.put( nvp.getName(), nvp.getValue() );
		String sign = new Sign( params ).getSign();
		rb.addParameter( "sign", sign );
	}
}
