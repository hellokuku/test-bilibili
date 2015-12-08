package org.xzc.bilibili.model;

import java.io.IOException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TestHttpClient {

	@Test
	public void test_String_To_JSONArray() {
		JSONArray ja = JSON.parseArray( "[1,\"abc\",'ceshi']" );
		System.out.println( ja );
	}

	@Test
	public void test_String_To_JSONObject() {
		Object obj = JSON.parse( "{name:'xzc',age:20}" );//实际返回是JSONObject
		System.out.println( obj );
	}

	@Test
	public void test_String_To_Object() {
		MyUser1 obj = JSON.parseObject( "{\"Age2\":20,\"id\":13,\"bir\":\"2015-12-06\",\"name\":\"xzc\"}",
				MyUser1.class );
		System.out.println( obj );
		//		obj = JSON.parseObject( "{ID:1,name:'xzc',age:20,password:'bzd',birthday:'2015--1--1'}", MyUser1.class );
		//		System.out.println( obj );
	}

	@Test
	public void test_Object_To_String() {
		MyUser1 u = new MyUser1();
		u.id = 1;
		u.name = "xzc";
		u.age = 20;
		u.birthday = new Date();
		u.password = "bzd";
		System.out.println( JSON.toJSONString( u ) );
	}

	@Test
	public void test_Object_To_JSONObject() {
		MyUser1 u = new MyUser1();
		u.id = 1;
		u.name = "xzc";
		u.age = 20;
		u.birthday = new Date();
		u.password = "bzd";
		System.out.println( JSON.toJSON( u ) );
		//		JSON.toJavaObject( json, clazz )
	}

	@Test
	public void test1() throws Exception {
		BasicCookieStore bcs = new BasicCookieStore();
		CloseableHttpClient hc = HttpClients.custom().setDefaultCookieStore( bcs ).build();
		long beg = System.currentTimeMillis();
		CloseableHttpResponse res = hc.execute( RequestBuilder.get( "http://www.baidu.com" ).build() );
		System.out.println( System.currentTimeMillis() - beg );
		EntityUtils.toString( res.getEntity() );
		HttpClientUtils.closeQuietly( res );
		System.out.println( System.currentTimeMillis() - beg );
		//System.out.println( result );
		HttpClientUtils.closeQuietly( hc );
	}
}
