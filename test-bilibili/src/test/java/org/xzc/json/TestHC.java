package org.xzc.json;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestHC {
	@Test
	public void test() throws Exception {
		HttpHost proxy = new HttpHost( "27.115.75.114", 8080 );
		CloseableHttpClient chc = HttpClients.custom().setProxy( proxy ).build();
		CloseableHttpResponse res = chc.execute( RequestBuilder.get( "http://1212.ip138.com/ic.asp" ).build() );
		String content = EntityUtils.toString( res.getEntity(), "gb2312" );
		System.out.println( content );
		res.close();
		chc.close();
	}
}
