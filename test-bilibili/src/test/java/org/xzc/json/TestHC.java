package org.xzc.json;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestHC {
	@Test
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
