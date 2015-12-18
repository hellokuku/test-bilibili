package org.xzc.json;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class TestHC {
	@Test
	public void test() throws Exception {
		CloseableHttpClient chc = HttpClients.custom().build();
		CloseableHttpResponse res = chc.execute( RequestBuilder.get( "http://www.baidu.com?name=xzc" ).build() );
		String content = EntityUtils.toString( res.getEntity() );
		URIBuilder ub = new URIBuilder( "http://www.baidu.com/?name=xzc" );
		System.out.println( ub.getQueryParams() );
		//RequestBuilder rb = RequestBuilder.get( "http://www.baidu.com/?name=xzc" );
		//System.out.println( rb.getParameters() );
		//System.out.println( content );
		res.close();
		chc.close();
	}
}
