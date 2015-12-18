package org.xzc.bilibili.api;

import java.io.IOException;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.util.HC;

import com.j256.ormlite.support.ConnectionSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DBConfig.class)
public class TestApi {
	private CloseableHttpClient chc;
	private HC hc;
	@Autowired
	private ConnectionSource cs;

	@Before
	public void before() {
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		chc = HttpClients.custom().setDefaultRequestConfig( rc ).build();
		hc = new HC( chc );
	}

	@After
	public void after() {
	}

	@Test
	public void test1() throws IOException {
		/*Sign sign = new Sign( "access_key", "5a1a64384ce4bcf7f668d6fa769f9e5d" );
		String result = hc.asString( RequestBuilder.post( "http://api.bilibili.com/x/share/first?" + sign.getResult() )
				.setEntity( new Params().add( "type", 21 ).add( "id", "3270784" ).toEntity() ).build() );
		System.out.println( result );*/
		String result = hc.postAsString( "http://api.bilibili.com/x/share/first",
				new Params( "access_key", "5a1a64384ce4bcf7f668d6fa769f9e5d" ), new Params( "type", 21, "id", 3270784 ),
				true );
		System.out.println( result );
		chc.close();
	}
}
