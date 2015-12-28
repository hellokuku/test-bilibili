package org.xzc.sina.email.auto;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.autoactive.AutoActiveRunner;
import org.xzc.bilibili.autosignin.AutoSignInRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.proxy.Proxy;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, 批量注册.class })
@Configuration
public class 批量注册 {
	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;
	@Autowired
	private RuntimeExceptionDao<Proxy, Integer> proxyDao;

	private static final String FIREFOX_PATH = "D:\\Program Files (x86)\\Firefox\\Firefox.exe";
	private RemoteWebDriver fd;
	//超时300秒
	private static final int TIMEOUT = 300;

	private static HC makeHC(String host, int port, boolean ignoreCookie) {
		return makeHC( 10000, host, port, ignoreCookie );
	}

	private static HC makeHC(int timeout, String host, int port, boolean ignoreCookie) {
		Builder b = RequestConfig.custom()
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout );
		if (ignoreCookie) {
			b.setCookieSpec( CookieSpecs.IGNORE_COOKIES );
		}
		RequestConfig rc = b.build();
		HttpHost proxy = null;
		if (host != null)
			proxy = new HttpHost( host, port );
		CloseableHttpClient chc = HttpClients.custom()
				.setProxy( proxy )
				.setDefaultRequestConfig( rc )
				.build();
		return new HC( chc );

	}

	private static HC makeHC() {
		return makeHC( true );
	}

	private static HC makeHC(boolean ignoreCookie) {
		return makeHC( 10000, null, 0, ignoreCookie );
	}

	private static HC makeHC(int timeout) {
		return makeHC( timeout, null, 0, true );
	}

	public void before() throws SQLException {
		before( null, 0 );
	}

	private FirefoxProfile fp;

	public void before(String proxy, int port) throws SQLException {
		fp = new FirefoxProfile();
		if (proxy != null) {
			fp.setPreference( "network.proxy.type", 1 );
			fp.setPreference( "network.proxy.http", proxy );
			fp.setPreference( "network.proxy.http_port", port );
			fp.setPreference( "network.proxy.share_proxy_settings", true );
			fp.setPreference( "network.proxy.no_proxies_on", "localhost" );
		} else {
			fp.setPreference( "network.proxy.type", 0 );
		}
		fp.setPreference( "browser.startup.homepage", "about:blank" );
		FirefoxBinary fb = new FirefoxBinary( new File( FIREFOX_PATH ) );
		fd = new FirefoxDriver( fb, fp );
	}

	@Test
	public void 步骤1_辅助注册新浪邮箱() throws InterruptedException, IOException, SQLException {
		before();
		fd.get( "http://1212.ip138.com/ic.asp" );
		System.out.println( fd.findElementByTagName( "center" ).getText() );
		accountLabel: for (int i = 0; i < 100; ++i) {
			try {
				fd.manage().deleteAllCookies();
				String username = RandomStringUtils.random( 10, true, false ).toLowerCase();
				fd.get( "https://mail.sina.com.cn/register/regmail.php" );
				fd.findElementByName( "email" ).sendKeys( username );
				fd.findElementByName( "psw" ).sendKeys( "70862045" );
				fd.findElementByName( "imgvcode" ).click();
				boolean ok = false;
				for (int j = 0; j < TIMEOUT; ++j) {
					String url = fd.getCurrentUrl();
					if (url.contains( "m0.mail.sina.com.cn/classic/index.php" )) {
						ok = true;
						break;
					} else if (url.contains( "mail.sina.com.cn/register/regmail.php" )) {
						List<WebElement> es = fd.findElementsByCssSelector( ".syserror_alp" );
						if (!es.isEmpty()) {
							if (es.get( 0 ).getText().contains( "注册疲劳" )) {
								System.out.println( "注册疲劳" );
								break accountLabel;
							}
						}
					}
					Thread.sleep( 100 );
				}
				if (!ok) {
					continue;
				}
				System.out.println( i + " " + username );
				String email = username + "@sina.com";
				String cookie = buildCookie( fd.manage().getCookies() );
				FileUtils.writeStringToFile( new File( EMAILS_COOKIES_FILENAME ), email + "\r\n" + cookie + "\r\n",
						true );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		after();
	}

	@Test
	public void 步骤1_辅助注册新浪邮箱_策略2() throws InterruptedException, IOException {
		BasicCookieStore bcs = new BasicCookieStore();
		RequestConfig rc = RequestConfig.custom().build();
		CloseableHttpClient chc = HttpClients.custom()
				//.setProxy( new HttpHost( "cache.sjtu.edu.cn", 8080) )
				.setDefaultRequestConfig( rc )
				.setDefaultCookieStore( bcs )
				.addInterceptorFirst( new HttpRequestInterceptor() {
					public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
						request.addHeader( "User-Agent",
								"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" );
						request.addHeader( "Referer", "https://mail.sina.com.cn/register/regmail.php" );
						request.addHeader( "Origin", "https://mail.sina.com.cn" );
					}
				} )
				.build();
		HC hc = new HC( chc );
		Scanner scanner = new Scanner( System.in );
		String username = RandomStringUtils.random( 10, true, false ).toLowerCase();
		String content = hc.getAsString( "https://mail.sina.com.cn/register/regmail.php" );
		Document d = Jsoup.parse( content );

		String showcode = d.select( "input[name=showcode]" ).val();
		String swfimgsk = d.select( "input[name=swfimgsk]" ).val();
		String forbin = d.select( "input[name=forbin]" ).val();
		String extcode = d.select( "input[name=extcode]" ).val();
		String email = username + "@sina.com";
		String psw = "70862045";
		while (true) {
			//下载图片
			byte[] vcode = hc.getAsByteArray( "https://mail.sina.com.cn/cgi-bin/imgcode.php" );
			FileUtils.writeByteArrayToFile( VCODE_FILE, vcode );
			System.out.println( "请输入验证码" );
			String imgvcode = scanner.nextLine();
			Req req = Req.post( "https://mail.sina.com.cn/register/regmail.php" )
					.datas(
							"act", 1,
							"agreement", "on",
							"showcode", showcode,
							"swfimgsk", swfimgsk,
							"forbin", forbin,
							"extcode", extcode,
							"email", email,
							"psw", psw,
							"imgvcode", imgvcode );
			content = hc.asString( req );
			JSONObject json = JSON.parseObject( content );
			int errno = json.getIntValue( "errno" );
			if (errno == 315)
				continue;
			if (errno == 0) {
				System.out.println( content );
				String url = json.getJSONObject( "data" ).getString( "url" );
				content = hc.getAsString( url );
				System.out.println( content );
				System.out.println( url );
				System.out.println( email );
				url = StringUtils.substringBetween( content, "arrURL\":[\"", "\",\"http:\\/\\/crosdom.weicaifu.com" );
				url = url.replace( "\\", "" );
				content = hc.getAsString( url );
				System.out.println( content );
				hc.getAsString( "http://m0.mail.sina.com.cn/classic/index.php" );
				String cookie = buildCookie( bcs.getCookies() );
				System.out.println( cookie );
			} else {
				System.out.println( json );
			}
			break;
		}
		scanner.close();
	}

	public void 新浪登陆() {

	}

	private static String buildCookie(List<org.apache.http.cookie.Cookie> cookies) {
		StringBuilder sb = new StringBuilder();
		for (org.apache.http.cookie.Cookie c : cookies) {
			sb.append( c.getName() + "=" + c.getValue() + ";" );
		}
		return sb.toString();
	}

	private static final String EMAILS_COOKIES_FILENAME = "emails_cookies.txt";
	private static final String URLS_FILENAME = "urls.txt";
	private static final File VCODE_FILE = new File( "vcode.jpg" );
	private static final File SENT_FILE = new File( "sent.txt" );

	@Deprecated
	public void 步骤2_辅助bilibili注册() throws Exception {
		before();
		List<String> lines = FileUtils.readLines( new File( EMAILS_COOKIES_FILENAME ) );
		List<String> sentList = FileUtils.readLines( SENT_FILE );
		for (int i = 0; i < lines.size(); i += 2) {
			String email = lines.get( i );
			if (sentList.contains( email )) {
				System.out.println( "跳过 " + email );
				continue;
			}
			while (true) {
				try {
					fd.manage().deleteAllCookies();
					fd.get( "https://account.bilibili.com/register/mail" );
					fd.findElementByName( "uname" ).sendKeys( email );
					fd.findElementByName( "agree" ).click();
					fd.findElementByName( "yzm" ).click();
					new WebDriverWait( fd, TIMEOUT )
							.until( ExpectedConditions
									.urlContains( "https://account.bilibili.com/register/mailsent" ) );
					System.out.println( email );
					FileUtils.writeStringToFile( new File( "sent.txt" ), email + "\n", true );
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		after();
	}

	@Test
	public void 步骤2_辅助bilibili注册_策略2() throws Exception {
		HC hc = makeHC( false );
		List<String> lines = FileUtils.readLines( new File( EMAILS_COOKIES_FILENAME ) );
		List<String> sentList = FileUtils.readLines( SENT_FILE );
		Scanner scanner = new Scanner( System.in );
		for (int i = 0; i < lines.size(); i += 2) {
			String email = lines.get( i );
			if (sentList.contains( email )) {
				System.out.println( "跳过 " + email );
				continue;
			}
			String url = "https://account.bilibili.com/register/mail";
			String vcodeUrl = "https://account.bilibili.com/captcha";
			String content = hc.getAsString( url );
			byte[] vcode = hc.getAsByteArray( vcodeUrl );
			FileUtils.writeByteArrayToFile( VCODE_FILE, vcode );
			System.out.println( "第" + ( i + 2 ) / 2 + "个 : " + email + " , 共有 " + lines.size() / 2 + "个" );
			System.out.println( "请输入验证码." );
			String yzm = scanner.nextLine();
			Req req = Req.post( url ).datas( "uname", email, "yzm", yzm, "agree", 1 );
			content = hc.asString( req );
			int status = -1;
			if (content.contains( "邮件已发送" ))
				status = 0;
			if (content.contains( "验证码错误 " ))
				status = 1;
			if (status == 0) {
				System.out.println( "邮件已发送" );
				FileUtils.writeStringToFile( SENT_FILE, email + "\n", true );
			} else if (status == 1) {
				System.out.println( "验证码错误" );
				i -= 2;
			} else {
				System.out.println( content );
				i -= 2;
			}
		}
		scanner.close();
	}

	private String buildCookie(Set<Cookie> set) {
		StringBuilder sb = new StringBuilder();
		for (Cookie c : set) {
			sb.append( c.getName() + "=" + c.getValue() + ";" );
		}
		return sb.toString();
	}

	@Test
	public void 步骤3_新浪邮箱进入后再激活() throws Exception {
		HC hc = makeHC();
		List<String> lines = FileUtils.readLines( new File( "emails_cookies.txt" ) );
		for (int i = 0; i < lines.size(); i += 2) {
			String email = lines.get( i );
			String cookie = lines.get( i + 1 );
			HttpUriRequest req = RequestBuilder.post( "http://m0.mail.sina.com.cn/wa.php?a=list_mail" )
					.addParameter( "fid", "all" )
					.addParameter( "order", "htime" )
					.addParameter( "sorttype", "desc" )
					.addParameter( "type", "0" )
					.addParameter( "pageno", "1" )
					.addParameter( "tag", "-1" )
					.addParameter( "webmail", "1" )
					.addHeader( "Cookie", cookie )
					.build();

			String content = hc.asString( req );
			JSONArray maillist = JSON.parseObject( content ).getJSONObject( "data" ).getJSONArray( "maillist" );
			for (int j = 0; j < maillist.size(); ++j) {
				JSONArray ja = maillist.getJSONArray( j );
				String title = ja.getString( 3 );
				if (title.contains( "哔哩哔哩" )) {
					String mid = ja.getString( 0 );
					req = RequestBuilder.get( "http://m0.mail.sina.com.cn/classic/readmail.php" )
							.addParameter( "webmail", "1" )
							.addParameter( "fid", "all" )
							.addParameter( "mid", mid )
							.addHeader( "Cookie", cookie )
							.build();
					content = hc.asString( req );
					content = JSON.parseObject( content ).getJSONObject( "data" ).getString( "body" );
					String url = StringUtils.substringBetween( content, "href=\"", "\" target=\"_blank\"" );
					//要激活的url
					FileUtils.writeStringToFile( new File( URLS_FILENAME ), url + "\r\n", true );
					System.out.println( url );
					break;
				}
			}
		}
	}

	private void 激活(Proxy p, List<String> urls) {
		HC hc = null;
		if (p == null)
			hc = makeHC( 5000 );
		else
			hc = makeHC( 5000, p.getIp(), p.getPort(), false );
		for (Iterator<String> iter = urls.iterator(); iter.hasNext();) {
			String url = iter.next();
			url = url.replace( "https", "http" );
			String email = StringUtils.substringBetween( url, "email=", "&time=" );
			String username = StringUtils.substringBefore( email, "@" );
			String password = "70862045";
			if (!dao.queryForEq( "userid", email ).isEmpty())
				continue;
			url = url.replace( "checkMail", "mailStep2" );

			RequestBuilder rb = RequestBuilder.post( url )
					.addParameter( "uname", username )
					.addParameter( "userpwd", password );
			if (p != null) {
				rb.addHeader( "X-Forwarded-For", p.getIp() )
						.addHeader( "X-Real-IP", p.getIp() )
						.addHeader( "X-Client-IP", p.getIp() )
						.addHeader( "X-Proxy-IP", p.getIp() )
						.addHeader( "Proxy-IP", p.getIp() )
						.addHeader( "Client-IP", p.getIp() )
						.addHeader( "Real-IP", p.getIp() );
			}
			HttpUriRequest req = rb.build();
			String content = hc.asString( req );
			int status = -1;
			if (content.contains( "注册成功" ))
				status = 0;
			if (content.contains( "已有该昵称" ))
				status = 1;
			if (content.contains( "本IP已经注册" ))
				status = 2;
			if (content.contains( "禁止使用代理进行注册" ))
				status = 3;
			if (status == 0 || status == 1) {
				Account a = new Account();
				a.userid = email;
				a.password = password;
				dao.create( a );
				iter.remove();
			} else if (status == 2 || status == 3) {
				if (status == 3) {
					System.out.println( "不能代理!" );
				}
				break;
			} else {
				System.out.println( content );
				break;
			}
			System.out.println( username + " " + status );
		}
	}

	@Test
	public void 步骤4_激活哔哩哔哩并绑定账号() throws Exception {
		QueryBuilder<Proxy, Integer> qb = proxyDao.queryBuilder();
		qb.where().eq( "success", true );
		qb.orderBy( "duration", true );
		List<Proxy> proxyList = qb.query();

		List<String> urls = FileUtils.readLines( new File( "urls.txt" ) );
		List<Account> accountList = dao.queryForAll();
		for (Account a : accountList) {
			for (Iterator<String> iter = urls.iterator(); iter.hasNext();) {
				String url = iter.next();
				if (url.contains( a.userid )) {
					iter.remove();
				}
			}
		}
		System.out.println( urls.size() );
		System.out.println( urls );
		boolean canGo = false;
		if (!urls.isEmpty()) {
			//激活( null, urls );
			for (Proxy p : proxyList) {
				System.out.println( "剩余="+urls.size()+" 使用代理 " + p );
				if (urls.isEmpty())
					break;
				if (true || p.getIp().equals( "" ))
					canGo = true;
				if (!canGo)
					continue;
				try {
					激活( p, urls );
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println( urls.size() );
	}

	@Test
	public void 步骤5_更新账号信息() {
		BilibiliService2 bs = new BilibiliService2();
		bs.setProxy( "202.195.192.197", 3128 );
		bs.postConstruct();
		List<Account> list = dao.queryForEq( "mid", 0 );
		for (Account a : list) {
			bs.clear();
			bs.login( a );
			Account aa = bs.getUserInfo();
			aa.userid = a.userid;
			aa.password = a.password;
			dao.update( aa );
			System.out.println( aa );
		}
	}

	@Test
	public void 步骤6_激活账号() {
		//请转
		Class clazz = AutoActiveRunner.class;
	}
	
	@Test
	public void 步骤7_赚积分() {
		//请转
		Class clazz = AutoSignInRunner.class;
	}
	

	@Test
	public void 其他问题修复() throws IOException {
		List<String> urls = FileUtils.readLines( new File( "urls.txt" ) );
		for (String url : urls) {
			url = url.replace( "https", "http" );
			String email = StringUtils.substringBetween( url, "email=", "&time=" );
			String username = StringUtils.substringBefore( email, "@" );
			String password = "70862045";
			if (!dao.queryForEq( "userid", email ).isEmpty())
				continue;
			Account a = new Account();
			a.userid = email;
			a.password = password;
			dao.create( a );
		}
	}

	@Test
	public void 随机字符串() {
		System.out.println( RandomStringUtils.random( 10, true, false ).toLowerCase() );
	}

	public void after() {
		fd.quit();
	}
}
