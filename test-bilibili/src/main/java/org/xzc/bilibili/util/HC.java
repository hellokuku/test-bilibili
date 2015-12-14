package org.xzc.bilibili.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 对hc的简单的封装
 * @author xzchaoo
 *
 */
public class HC {
	private CloseableHttpClient hc;

	public HC(CloseableHttpClient hc) {
		this.hc = hc;
	}

	public String getAsString(String url) {
		return asString( RequestBuilder.get( url ).build() );
	}

	public JSONObject getAsJSON(String url) {
		return asJSON( RequestBuilder.get( url ).build() );
	}

	public JSONObject asJSON(HttpUriRequest req) {
		return JSON.parseObject( asString( req ) );
	}

	public String asString(final HttpUriRequest req) {
		return safeRun( new SafeRunner<String>() {
			public String run() throws Exception {
				CloseableHttpResponse res = null;
				try {
					res = hc.execute( req );
					String content = EntityUtils.toString( res.getEntity(), "utf-8" );
					return content;
				} finally {
					HttpClientUtils.closeQuietly( res );
				}
			}
		} );
	}

	private static <T> T safeRun(SafeRunner<T> sr) {
		try {
			return sr.run();
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException( e );
		}
	}

	public void close() {
		HttpClientUtils.closeQuietly( hc );
	}

	public static UrlEncodedFormEntity makeFormEntity(Object... args) {
		if (args == null || args.length == 0)
			return null;
		if (args.length % 2 != 0)
			throw new IllegalArgumentException( "数组大小必须为偶数" );
		List<NameValuePair> params = new ArrayList<NameValuePair>( args.length / 2 );
		for (int i = 0; i < args.length; i += 2) {
			String name = args[i].toString();
			String value = args[i + 1].toString();
			params.add( new BasicNameValuePair( name, value ) );
		}
		UrlEncodedFormEntity e = null;
		try {
			e = new UrlEncodedFormEntity( params, "utf-8" );
		} catch (Exception ex) {
		}
		return e;
	}
}
