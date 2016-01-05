package org.xzc.bilibili.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.util.HCs;
import org.xzc.http.HC;
import org.xzc.http.Params;
import org.xzc.http.Req;

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
		HttpHost proxy = new HttpHost( "202.195.192.197", 3128 );
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
	public void testStr() {
		for (int i = 0; i < 10; ++i) {
			System.out.println( RandomStringUtils.random( 6, true, false ).toLowerCase() );
		}
	}

	@Test
	public void testXXX() throws InterruptedException {
		HttpUriRequest req = RequestBuilder.post( "http://api.bilibili.com/x/reply/del" )
				.setEntity( new Params( "oid", 3448994, "type", 1, "rpid", 74131349, "reason", 3, "content", "刷屏" )
						.toEntity() )
				.addHeader( "Cookie", "DedeUserID=19557477; SESSDATA=dba5edc0%2C1453632378%2C974d1bad;" )
				.build();
		while (true) {
			String content = hc.asString( req );
			System.out.println( content );
			Thread.sleep( 1000 );
		}
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

	@Test
	public void 某邮箱注册() throws IOException {
		HC hc = HCs.makeHC( false );
		String username = "ceshiceshi7";
		String password = "70862045";
		Req req = Req.get( "http://mail.hainan.net/webmailhainan/tianyaValidateCode.jsp" )
				.header( "Referer", "http://mail.hainan.net/webmailhainan/register.jsp" );
		FileUtils.writeByteArrayToFile( new File( "vcode_0.png" ),
				hc.asByteArray( req ) );
		Scanner scanner = new Scanner( System.in );
		System.out.println( "请输入验证码" );
		String vcode = scanner.nextLine();

		req = Req.post( "http://mail.hainan.net/webmailhainan/hn_adduser.jsp" )
				.datas( "email_text", username,
						"email_type", "hainan.net",
						"pwd", password,
						"pwd_again", password,
						"phone_number", "",
						"codetext", vcode );
		CloseableHttpResponse res = hc.asRes( req );
		try {
			int code = res.getStatusLine().getStatusCode();
			if (code == 200) {
				String content = EntityUtils.toString( res.getEntity() );
				System.out.println( "验证码不对" );
			} else if (code == 302) {
				Header h = res.getFirstHeader( "Location" );
				if (h != null) {
					String location = h.getValue();
					if (location.startsWith( "http://mail.hainan.net/webmailhainan/login.jsp" )) {
						System.out.println( URI.create( location ).getQuery() );
					} else if (location.startsWith( "http://mail.hainan.net/webmailhainan/login_submit.jsp" )) {
						System.out.println( "注册成功" );
					} else {
						System.out.println( "注册失败 " + location );
					}
				}
			} else {
				System.out.println( "其他问题" );
			}
		} finally {
			HttpClientUtils.closeQuietly( res );
		}
		/*
		req = Req.get( "http://mail.hainan.net/webmailhainan/login_submit.jsp" )
			.params( "doLogin", true,
					"username", username,
					"password", password,
					"redirectStr", "mail.jsp",
					"hostname", "hainan.net" );
		System.out.println( req.build().getURI().toString() );
		String content = hc.asString( req );
		System.out.println( content );
		if (!content.contains( "请您输入密保" )) {
		System.out.println( content );
		System.out.println( "第1步失败" );
		return;
		}
		//重定向到get http://mail.hainan.net/webmailhainan/login_submit.jsp?doLogin=true&username=cehiceshi2&password=70862045&redirectStr=mail.jsp&hostname=hainan.net
		//重定向到 http://mail.hainan.net/webmailhainan/passwordprotection/web/hainan_mibao.jsp
		req = Req.post( "http://mail.hainan.net/webmailhainan/passwordprotection/web/hainanMibaoResult.jsp" )
			.datas( new Params(
					"psd_question", "您最喜欢的数字是？",
					"psd_answer", "1",
					"pwd_ok_btn", "确定" ).encoding( "gb2312" ) );
		content = hc.asString( req );
		if (!( content.contains( "您已成功设置密保" ) || content.contains( "您已设置过密保" ) )) {
		System.out.println( "第2步失败" );
		System.out.println( content );
		return;
		}
		/*req = Req.get( "http://mail.hainan.net/webmailhainan/passwordprotection/web/raffleHainanyou.jsp?username="
			+ username );
		content = hc.asString( req );
		if (!content.contains( "0,0,*,0,0,0,0,0,0" )) {
		System.out.println( "第3步失败" );
		return;
		}*/
	}

	@Test
	public void 某邮箱登陆() throws IOException {
		HC hc = HCs.makeHC( false );
		String username = "ceshiceshi5";
		String password = "70862045";
		Req req = Req.post( "http://mail.hainan.net/webmailhainan/login_submit.jsp" )
				.params( "doLogin", true,
						"redirectStr", "Index",
						"username", username,
						"hostname", "hainan.net",
						"password", password,
						"x", 45, "y", 28 );
		CloseableHttpResponse res = hc.asRes( req );
		int loginResult = -1;
		try {
			int code = res.getStatusLine().getStatusCode();
			if (code == 302) {
				String location = res.getFirstHeader( "Location" ).getValue();
				if (location.equals( "http://mail.hainan.net/webmailhainan/Index" )) {
					loginResult = 1;
				} else if (location
						.equals( "http://mail.hainan.net/webmailhainan/passwordprotection/web/hainan_mibao.jsp" )) {
					loginResult = 2;
				}
			}
		} finally {
			HttpClientUtils.closeQuietly( res );
		}
		System.out.println( "登陆结果 " + loginResult );
		if (loginResult == 2) {
			req = Req.post( "http://mail.hainan.net/webmailhainan/passwordprotection/web/hainanMibaoResult.jsp" )
					.datas( new Params(
							"psd_question", "您最喜欢的数字是？",
							"psd_answer", "1",
							"pwd_ok_btn", "确定" ).encoding( "gb2312" ) );
			String content = hc.asString( req );
			if (!( content.contains( "您已成功设置密保" ) || content.contains( "您已设置过密保" ) )) {
				System.out.println( "密保步骤失败" );
				System.out.println( content );
				return;
			}
			loginResult = 1;
		}
		if (loginResult == 1) {
			String content = hc.getAsString( "http://mail.hainan.net/webmailhainan/mailfolder.jsp" );
			String fn = StringUtils.substringBetween( content,
					"o.chkName=\"chk-0_verify%40mail.bilibili.tv\";o.chkValue=\"",
					"|new\";o.popBKColor=" );
			if (fn != null) {
				req = Req.get( "http://mail.hainan.net/webmailhainan/mailshow.jsp" )
						.params( "mid", fn, "fid", "new" );
				hc.consume( req );
				req = Req.get( "http://mail.hainan.net/webmailhainan/mailshowpart.jsp" )
						.params( "fn", fn + ".internal.html", "mid", fn, "charset", "UTF-8" );
				hc.consume( req );
				content = hc.asString( req );
				String url = StringUtils.substringBetween( content, "<a href=\"", "\"" );
				System.out.println( url );
			} else {
				System.out.println( "没有找到信" );
			}
		}
		req = Req.post( "http://mail.hainan.net/webmailhainan/logout.jsp" );
		hc.consume( req );
	}
}
