package org.xzc.bilibili.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

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

	private String turl = "http://api.bilibili.com/feedback?page=1&mode=arc&type=json&ver=3&order=default&pagesize=1&aid=3359166";

	@Test
	public void test1() throws Exception {
		BasicCookieStore bcs = new BasicCookieStore();
		HttpHost proxy = new HttpHost( "cache.sjtu.edu.cn", 8080 );
		CloseableHttpClient hc = HttpClients.custom().setProxy( proxy ).setDefaultCookieStore( bcs ).build();
		//CloseableHttpClient hc = HttpClients.custom().setDefaultCookieStore( bcs ).build();
		long beg = System.currentTimeMillis();
		CloseableHttpResponse res = hc.execute( RequestBuilder.get( turl ).build() );
		String content = EntityUtils.toString( res.getEntity(), "utf8" );
		HttpClientUtils.closeQuietly( res );
		//System.out.println( result );
		HttpClientUtils.closeQuietly( hc );
		//System.out.println( content );
		System.out.println( content.length() );
		System.out.println( System.currentTimeMillis() - beg );
		System.out.println( res.getStatusLine().getStatusCode() );
		for (Header h : res.getAllHeaders()) {
			System.out.println( h.toString() );
		}
	}

	@Test
	public void test2() throws Exception {
		long beg = System.currentTimeMillis();
		URL url = new URL( turl );
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty( "Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" );
		con.setRequestProperty( "Accept", "Accept-Language:zh-CN,zh;q=0.8" );
		con.setRequestProperty( "User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" );
		System.out.println( con.getContentType() );
		System.out.println( con.getContentEncoding() );
		StringBuffer sb = new StringBuffer();
		InputStream is = con.getInputStream();
		//		BufferedReader br = new BufferedReader( new InputStreamReader( new GZIPInputStream( is ), "utf-8" ) );
		BufferedReader br = new BufferedReader( new InputStreamReader( is, "GBK" ) );
		String line = null;
		while (( line = br.readLine() ) != null) {
			sb.append( line );
		}
		String content = sb.toString();
		//System.out.println( content );
		System.out.println( content.length() );
		br.close();
		is.close();
		long end = System.currentTimeMillis();
		System.out.println( end - beg );
	}
}
