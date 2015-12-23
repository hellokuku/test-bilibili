package org.xzc.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestHC {

	@Test
	public void testxxx() {
		Set<String> set = new TreeSet<String>( Arrays.asList(
				"125.39.7.139",
				"124.95.153.199",
				"14.136.134.118",
				"14.152.58.20",
				"58.220.29.10",
				"124.95.153.199",
				"124.95.153.199",
				"122.225.39.202",
				"121.17.17.90",
				"113.105.152.207",
				"61.179.50.21",
				"124.95.153.199",
				"211.161.102.180",
				"121.17.17.90",
				"61.179.50.21",
				"61.179.50.21",
				"182.118.9.10",
				"58.220.29.10",
				"124.95.153.199",
				"119.84.82.202",
				"221.234.38.238",
				"61.179.50.21",
				"122.225.39.202",
				"113.105.152.207",
				"211.161.102.180",
				"14.136.134.118",
				"125.39.7.139",
				"124.95.153.199",
				"124.95.153.199",
				"61.179.50.21",
				"119.84.82.202",
				"122.225.39.202",
				"220.194.222.7",
				"221.234.38.238",
				"119.84.82.202",
				"218.76.137.149",
				"14.136.134.118",
				"125.39.7.139",
				"122.225.39.202",
				"47.88.138.238",
				"122.225.39.202",
				"119.84.82.202",
				"112.25.85.6",
				"113.105.152.207",
				"113.105.152.207",
				"211.161.102.180",
				"14.136.134.118",
				"183.61.9.45",
				"183.61.9.45",
				"47.88.138.238",
				"122.225.39.202",
				"14.136.134.118",
				"107.182.165.170",
				"14.136.134.118",
				"14.136.134.118",
				"183.61.9.45",
				"192.161.173.58",
				"221.234.38.238",
				"218.205.74.9",
				"183.203.29.34",
				"122.225.39.202",
				"218.205.74.9",
				"218.76.137.149",
				"111.23.6.114",
				"183.61.9.45",
				"122.225.39.202",
				"121.17.17.90",
				"220.194.222.7",
				"124.95.153.199",
				"122.225.39.202",
				"218.205.74.9",
				"183.203.29.34",
				"183.203.29.34",
				"117.34.100.9",
				"218.205.74.9",
				"218.205.74.9" ) );
		for (String s : set) {
			System.out.println( "\"" + s + "\"," );
		}
	}

	public void test() throws Exception {
		HttpHost proxy = new HttpHost( "222.35.17.177", 2076 );
		CloseableHttpClient chc = HttpClients.custom().addInterceptorFirst( new HttpRequestInterceptor() {
			public void process(HttpRequest req, HttpContext ctx) throws HttpException, IOException {
				req.addHeader( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" );
				req.addHeader( "Host", "api.bilibili.com" );
			}
		} ).setProxy( proxy ).build();
		String url = "http://1212.ip138.com/ic.asp";
		String url2 = "http://113.105.152.207/userinfo?user=xzchaooDRF8";
		CloseableHttpResponse res = chc.execute( RequestBuilder.get( url2 ).build() );
		String content = EntityUtils.toString( res.getEntity(), "gb2312" );
		System.out.println( content );
		res.close();
		chc.close();
	}
}
