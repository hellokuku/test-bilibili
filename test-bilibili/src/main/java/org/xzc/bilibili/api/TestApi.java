package org.xzc.bilibili.api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.util.HC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DBConfig.class)
public class TestApi {
	private CloseableHttpClient chc;
	private HC hc;

	@Before
	public void before() {
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( 200 );
		p.setDefaultMaxPerRoute( 100 );
		HttpHost proxy = new HttpHost( "40.118.131.11", 8080 );
		proxy = null;
		chc = HttpClients.custom().setProxy( proxy ).setDefaultRequestConfig( rc ).setConnectionManager( p ).build();
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
					System.out.println( content );
					/*
					if (unicode) {
						content = Utils.decodeUnicode( content );
					}
					System.out.println( tag + " " + DateTime.now().toString() + " " + content );
					if (content.contains( "OK" ) || content.contains( "验证码" ) || content.contains( "禁言" ))
						break;*/
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

	private static HttpUriRequest makeCommentRequest3(String DedeUserId, String SESSDATA, String aid, String msg) {
		return RequestBuilder.post( "http://61.164.47.167/x/reply/add" )
				.addHeader( "Host", "api.bilibili.com" )
				.addHeader( "Origin", "http://www.bilibili.com" )
				.addHeader( "Pragma", "no-cache" )
				.addHeader( "Cache-Control", "no-cache" )
				.addHeader( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" )
				.addHeader( "Cookie", "DedeUserID=" + DedeUserId + "; SESSDATA=" + SESSDATA + ";" )
				.addHeader( "Referer", "http://www.bilibili.com/video/av" + aid + "/" )
				.addHeader( "X-Real-IP", "1.2.4.8" )
				.addHeader( "Real-IP", "1.2.4.8" )
				.addHeader( "X-Forwarded-For", "1.2.4.8" )
				.addHeader( "X-Client-IP", "1.2.4.8" )
				.addHeader( "Client-IP", "1.2.4.8" )
				.addHeader( "X-Proxy-IP", "1.2.4.8" )
				.addHeader( "Proxy-IP", "1.2.4.8" )
				.addHeader( "Remote-Addr", "1.2.4.8" )
				.setEntity(
						new Params( "jsonp", "json", "message", msg, "oid", aid, "type", 1 ).toEntity() )
				.build();
	}

	public void testZan() {
		HttpUriRequest req = RequestBuilder.post( "http://api.bilibili.com/x/reply/action" )
				.setEntity( new Params( "jsonp", "json", "oid", 3367069, "type", 1, "rpid", 72852618, "action", 1 )
						.toEntity() )
				.addHeader( "Cookie", "DedeUserID=19557477; SESSDATA=dba5edc0%2C1453529228%2Cc520ae0a;" )
				.build();
		String content = hc.asString( req );
		System.out.println( content );
	}

	private ExecutorService es;// = Executors.newFixedThreadPool( 1024 );

	private AtomicInteger count = new AtomicInteger( 0 );
	private AtomicInteger total = new AtomicInteger( 0 );
	private List<String> wordList;

	private void doAction(final String action, final int z) {
		count.incrementAndGet();
		es.submit( new Runnable() {
			public void run() {
				int total0 = total.incrementAndGet();
				if (total0 % 500000 == 0) {
					System.out.println( total0 + " " + action );
				}
				HttpUriRequest req = RequestBuilder.post( "http://api.bilibili.com/x" + action ).build();
				String content = hc.asString( req ).trim();
				if (!content.equals( "404 page not found" )) {
					System.out.println( action );
				}
				count.decrementAndGet();
			}
		} );
	}

	private void dfs(String action, int z) throws InterruptedException {
		if (z == 2)
			return;
		for (String w : wordList) {
			String na = action + "/" + w;
			while (count.get() > 100000) {
				Thread.sleep( 1000 );
			}
			doAction( na, z + 1 );
			dfs( na, z + 1 );
		}
	}

	/*add
	del
	hide
	info
	jump
	show*/
	public void findReply() throws InterruptedException, IOException, URISyntaxException {
		wordList = FileUtils.readLines( new File( getClass().getClassLoader().getResource( "words.txt" ).toURI() ) );
		dfs( "", 0 );
		es.awaitTermination( 1, TimeUnit.DAYS );
		es.shutdown();
	}

	@Test
	public void testShareFirst() {
		HttpUriRequest req = RequestBuilder.post( "http://api.bilibili.com/x/share/first" )
				.setEntity( new Params(
						"id", 45229,
						"aid", 45229,
						"typeid", 20,
						"type", 1 )
								.toEntity() )
				.addHeader( "Cookie", "DedeUserID=19557477; SESSDATA=dba5edc0%2C1453529228%2Cc520ae0a;" )
				.build();
		String content = hc.asString( req );
		System.out.println( content );
	}

	public void testReplyDel() {
		HttpUriRequest req = RequestBuilder.post( "http://api.bilibili.com/x/reply/del" )
				.setEntity( new Params(
						//"id", 73799661, "aid", 45229,
						//"rpid", 73799661, "oid", 45229,
						"rpid", 73799661,
						"oid", 45229,
						"type", 1,
						"message", "ceshiceshiceshi"
		//"mid", 19557477,
		//"reason", "asdfkjsdfjsdjfkslj"
		)
				.toEntity() )
				.addHeader( "Cookie", "DedeUserID=19557477; SESSDATA=dba5edc0%2C1453529228%2Cc520ae0a;" )
				.build();
		String content = hc.asString( req );
		System.out.println( content );
	}

	public void testCookie() throws Exception {
		HttpUriRequest req = RequestBuilder.post( "https://account.bilibili.com/ajax/miniLogin/login" ).setEntity(
				new Params( "userid", "duruofeixh7@163.com", "pwd", "xzc@7086204511", "keep", 1 ).toEntity() ).build();
		String result = hc.asString( req );

		JSONObject json = JSON.parseObject( result );
		if (json.getBooleanValue( "status" )) {
			URIBuilder b = new URIBuilder( json.getJSONObject( "data" ).getString( "crossDomain" ) );
			List<NameValuePair> list = b.getQueryParams();
			System.out.println( list.size() );
			System.out.println( list );
		} else {
			System.out.println( "登录失败" );
		}
		System.out.println( result );
	}

	public void test2() throws Exception {
		// "19480366", "f3e878e5,1451143184,7458bb46",
		//HttpUriRequest req1 = makeCommentRequest1( "19557513", "315c6283%2C1451530664%2C92401ca4", "45229",
		//		"测试测试测试测1" );
		//HttpUriRequest req2 = makeCommentRequest2( "19557513", "315c6283%2C1451530664%2C92401ca4", "45229",
		//		"测试测试测试测2" );
		//只跟SESSDATA有关!
		HttpUriRequest req3 = makeCommentRequest3( "0", "315c6283%2C1451530664%2C92401ca4", "45229",
				"2" );
		System.out.println( hc.asString( req3 ) );
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
