package org.xzc.bilibili.api;

import java.io.IOException;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.util.HC;
import org.xzc.bilibili.util.Utils;

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
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( 200 );
		p.setDefaultMaxPerRoute( 100 );
		chc = HttpClients.custom().setDefaultRequestConfig( rc ).setConnectionManager( p ).build();
		hc = new HC( chc );
	}

	@After
	public void after() {
	}

	private static HttpUriRequest makeCommentRequest1(String DedeUserId, String SESSDATA, String aid, String msg) {
		return RequestBuilder.get( "http://interface.bilibili.com/feedback/post" )
				.addHeader( "Cookie", "DedeUserID=" + DedeUserId + "; SESSDATA=" + SESSDATA + ";" )
				.addHeader( "Host", "interface.bilibili.com" )
				.addParameter( "callback", "abc" )
				.addParameter( "aid", aid )
				.addParameter( "msg", msg )
				.addParameter( "action", "send" )
				.addHeader( "Referer", "http://www.bilibili.com/video/av" + aid )
				.build();
	}

	private Thread run0(final String tag, final HttpUriRequest req, final boolean unicode) {
		Thread t = new Thread() {
			public void run() {
				while (true) {
					String content = hc.asString( req ).trim();
					if (unicode) {
						content = Utils.decodeUnicode( content );
					}
					System.out.println( tag + " " + DateTime.now().toString() + " " + content );
					if (content.contains( "OK" ) || content.contains( "验证码" ) || content.contains( "禁言" ))
						break;
				}
			}
		};
		t.start();
		return t;
	}

	private static HttpUriRequest makeCommentRequest2(String DedeUserId, String SESSDATA, String aid, String msg) {
		return RequestBuilder.post( "http://www.bilibili.com/feedback/post" )
				.addHeader( "User-Agent", "Mozilla/5.0 BiliDroid/2.3.4 (bbcallen@gmail.com)" )
				.addHeader( "Cookie", "DedeUserID=" + DedeUserId + "; SESSDATA=" + SESSDATA + ";" )
				.addHeader( "Referer", "http://www.bilibili.com" )
				.setEntity(
						new Params( "type", "jsonp", "callback", "abc", "aid", aid, "msg", msg, "platform",
								"android"/*, "appkey", "03fc8eb101b091fb"*/ )
										.toEntity() )
				.build();
	}

	@Test
	public void test2() throws Exception {
		HttpUriRequest req1 = makeCommentRequest1( "19480366", "f3e878e5,1451143184,7458bb46", "3407473", "测试测试测试测" );
		HttpUriRequest req2 = makeCommentRequest2( "19480366", "f3e878e5,1451143184,7458bb46", "3407473", "测试测试测试测" );
		for (int i = 0; i < 1; ++i) {
			//Thread t1 = run0( "[1]", req1, true );
			Thread t2 = run0( "[2]", req2, false );
		}

		Thread.sleep( 100000 );
		//t1.join();
		//t2.join();
	}

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
