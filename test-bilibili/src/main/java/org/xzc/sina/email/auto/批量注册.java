package org.xzc.sina.email.auto;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
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
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
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
import org.xzc.bilibili.util.HCs;
import org.xzc.http.HC;
import org.xzc.http.Params;
import org.xzc.http.Req;
import org.xzc.vcode.PositionManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
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

	private static Method _getElementp = null;

	private static DomElement getElement(HtmlUnitWebElement e) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (_getElementp == null) {
			_getElementp = e.getClass().getDeclaredMethod( "getElement" );
			_getElementp.setAccessible( true );
		}
		DomElement de = (DomElement) _getElementp.invoke( e );
		return de;
	}

	@Test
	public void 步骤1_辅助注册海南邮箱_策略6() throws Exception {
		int batch=3;
		PositionManager pm = new PositionManager();
		pm.setBatch( batch );
		pm.init();
		ExecutorService es = Executors.newFixedThreadPool( batch );
		final AtomicInteger count = new AtomicInteger( 0 );
		Step1Callback2 cb = new Step1Callback2() {
			public void callback(String email, String password, String cookie, int status) throws Exception {
				if (status == 0) {
					System.out.println( email + " " + password + " " + count.incrementAndGet() );
					FileUtils.writeStringToFile( new File( EMAILS_COOKIES_FILENAME ),
							email + " " + password + "\r\n" + cookie + "\r\n",
							true );
				} else {
					System.out.println( "状态=" + status );
				}
			}
		};

		for (int i = 0; i < batch; ++i) {
			Step1Worker3 sw = new Step1Worker3( "Worker" + i, pm, es, cb );
			sw.initAsync();
		}
		pm.loop();
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
	}

	@Test
	public void 步骤1_辅助注册新浪邮箱_策略5() throws Exception {
		int batch = 9;
		PositionManager pm = new PositionManager();
		pm.setBatch( batch );
		pm.init();
		ExecutorService es = Executors.newFixedThreadPool( 16 );
		final AtomicInteger count = new AtomicInteger( 0 );
		Step1Callback2 cb = new Step1Callback2() {
			public void callback(String email, String password, String cookie, int status) throws Exception {
				if (status == 0) {
					System.out.println( email + " " + password + " " + count.incrementAndGet() );
					FileUtils.writeStringToFile( new File( EMAILS_COOKIES_FILENAME ),
							email + " " + password + "\r\n" + cookie + "\r\n",
							true );
				} else {
					System.out.println( "状态=" + status );
				}
			}
		};

		for (int i = 0; i < 16; ++i) {
			Step1Worker2 sw = new Step1Worker2( "Worker" + i, pm, es, cb );
			sw.initAsync();
		}
		pm.loop();
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
	}

	@Test
	public void 步骤1_辅助注册新浪邮箱_策略4() throws Exception {
		LinkedList<Step1Worker> swlist = new LinkedList<Step1Worker>();
		int batch = 9;
		ExecutorService es = Executors.newFixedThreadPool( batch );
		final AtomicBoolean stop = new AtomicBoolean( false );
		for (int i = 0; i < batch; ++i) {
			HtmlUnitDriver d = new HtmlUnitDriver( BrowserVersion.FIREFOX_38, true );
			Step1Worker sw = new Step1Worker( d, new File( "vcode_" + i + ".png" ), es );
			sw.init();
			swlist.add( sw );
		}
		final AtomicInteger count = new AtomicInteger( 0 );
		Scanner scanner = new Scanner( System.in );
		while (!stop.get()) {
			for (Step1Worker sw : swlist) {
				if (stop.get())
					break;
				synchronized (sw.lock) {
					System.out.println( "请输入验证码 " + sw.vcode.getName() );
					String vcode = scanner.nextLine();
					sw.doAfter( vcode, new Step1Callback() {
						public void onResult(Step1Worker sw, int status) throws IOException {
							//if (status == 1)
							//	stop.set( true );
							if (status == 0) {
								System.out.println( sw.username + " " + sw.password + " " + count.incrementAndGet() );
								String cookie = buildCookie( sw.d.manage().getCookies() );
								String email = sw.username + "@sina.com";
								String password = sw.password;
								FileUtils.writeStringToFile( new File( EMAILS_COOKIES_FILENAME ),
										email + " " + password + "\r\n" + cookie + "\r\n",
										true );
							} else {
								System.out.println( "状态=" + status );
							}
						}
					} );
				}
			}
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		for (Step1Worker sw : swlist)
			sw.d.quit();
		scanner.close();
	}

	@Test
	public void 步骤1_辅助注册新浪邮箱_策略3() throws Exception {
		HtmlUnitDriver fd = new HtmlUnitDriver( BrowserVersion.FIREFOX_38, true );
		fd.get( "http://1212.ip138.com/ic.asp" );
		System.out.println( fd.findElementByTagName( "center" ).getText() );
		Scanner scanner = new Scanner( System.in );
		accountLabel: for (int i = 0; i < 100; ++i) {
			try {
				fd.manage().deleteAllCookies();
				String username = RandomStringUtils.random( 10, true, false ).toLowerCase();
				String password = RandomStringUtils.random( 10, true, false ).toLowerCase();
				fd.get( "https://mail.sina.com.cn/register/regmail.php" );
				fd.findElementByName( "email" ).sendKeys( username );
				fd.findElementByName( "psw" ).sendKeys( password );

				HtmlUnitWebElement yzm = (HtmlUnitWebElement) fd.findElementById( "capcha" );
				HtmlImage hi = (HtmlImage) getElement( yzm );
				String vcodeUrl = hi.getSrcAttribute();
				hi.saveAs( VCODE_FILE );
				System.out.println( "请输入验证码" );
				String vcode = scanner.nextLine();
				fd.findElementByName( "imgvcode" ).sendKeys( vcode );
				fd.findElementByCssSelector( ".subIco" ).click();

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
						es = fd.findElementsByCssSelector( ".tipInfor > abbr" );
						if (!es.isEmpty()) {
							WebElement we = es.get( es.size() - 1 );
							if (we.getText().contains( "输入错误" )) {
								System.out.println( "输入错误" );
								String newvcodeUrl = hi.getSrcAttribute();
								if (!vcodeUrl.equals( newvcodeUrl )) {
									vcodeUrl = newvcodeUrl;
									hi.saveAs( VCODE_FILE );
									System.out.println( "请输入验证码" );
									vcode = scanner.nextLine();
									fd.findElementByName( "imgvcode" ).clear();
									fd.findElementByName( "imgvcode" ).sendKeys( vcode );
									fd.findElementByCssSelector( ".subIco" ).click();
								}
							}
						}
					}
					Thread.sleep( 100 );
				}
				if (!ok) {
					continue;
				}
				System.out.println( i + " " + username + " " + password );
				String email = username + "@sina.com";
				String cookie = buildCookie( fd.manage().getCookies() );
				FileUtils.writeStringToFile( new File( EMAILS_COOKIES_FILENAME ),
						email + " " + password + "\r\n" + cookie + "\r\n",
						true );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fd.quit();
	}

	@Test
	public void 步骤1_辅助注册新浪邮箱() throws InterruptedException, IOException, SQLException {
		before();
		fd.get( "http://1212.ip138.com/ic.asp" );
		System.out.println( fd.findElementByTagName( "center" ).getText() );
		HC hc = HCs.makeHC();
		Scanner scanner = new Scanner( System.in );
		accountLabel: for (int i = 0; i < 100; ++i) {
			try {
				fd.manage().deleteAllCookies();
				String username = RandomStringUtils.random( 10, true, false ).toLowerCase();
				String password = RandomStringUtils.random( 10, true, false ).toLowerCase();
				fd.get( "https://mail.sina.com.cn/register/regmail.php" );
				fd.findElementByName( "email" ).sendKeys( username );
				fd.findElementByName( "psw" ).sendKeys( password );
				String cookie = null;
				/*
				cookie = buildCookie( fd.manage().getCookies() );
				byte[] vcodeData = hc.asByteArray( RequestBuilder.get("https://mail.sina.com.cn/cgi-bin/imgcode.php").addHeader( "Cookie", cookie).build() );
				FileUtils.writeByteArrayToFile( VCODE_FILE, vcodeData );
				System.out.println( "请输入验证码" );
				String vcode = scanner.nextLine();
				fd.findElementByName( "imgvcode" ).sendKeys( vcode);
				fd.findElementByCssSelector( ".subIco" ).click();
				//new WebDriverWait( fd, 30 ).until( ExpectedConditions. )
				WebElement we = fd.findElementById( "capcha" );
				BufferedImage bi = ImageIO.read( fd.getScreenshotAs( OutputType.FILE));
				BufferedImage bi2 = bi.getSubimage( we.getLocation().x, we.getLocation().y, we.getSize().width, we.getSize().height );
				ImageIO.write( bi2, "png",VCODE_FILE);
				*/
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
						/*es=fd.findElementsByCssSelector( ".tipInfor > abbr" );
						if(!es.isEmpty()){
							System.out.println( "发现tipInfor"+ es.get( 0 ).getText() );
							if(es.get( 0 ).getText().contains( "输入错误" )){
								cookie = buildCookie( fd.manage().getCookies() );
								vcodeData = hc.asByteArray( RequestBuilder.get().addHeader( "Cookie", cookie).build() );
								FileUtils.writeByteArrayToFile( VCODE_FILE, vcodeData );
								System.out.println( "请输入验证码" );
								vcode = scanner.nextLine();
								fd.findElementByName( "imgvcode" ).sendKeys( vcode);
								fd.findElementByCssSelector( ".subIco" ).click();
							}
						}*/
					}
					Thread.sleep( 100 );
				}
				if (!ok) {
					continue;
				}
				System.out.println( i + " " + username + " " + password );
				String email = username + "@sina.com";
				cookie = buildCookie( fd.manage().getCookies() );
				FileUtils.writeStringToFile( new File( EMAILS_COOKIES_FILENAME ),
						email + " " + password + "\r\n" + cookie + "\r\n",
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
	private static final File VCODE_FILE = new File( "vcode.jpg" );
	private static final File SENT_FILE = new File( "sent.txt" );
	protected static final File VCODE_FILE_1 = new File( "vcode_1.jpg" );
	protected static final File VCODE_FILE_2 = new File( "vcode_2.jpg" );
	private static final File URLS_FILE = new File( "urls.txt" );
	private static final File EMAILS_COOKIES_FILE = new File( "emails_cookies.txt" );

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

		List<String> lines = FileUtils.readLines( new File( EMAILS_COOKIES_FILENAME ) );
		final Set<String> sentSetist = Collections
				.synchronizedSet( new HashSet<String>( FileUtils.readLines( SENT_FILE ) ) );
		List<String> emails = new ArrayList<String>();

		for (int i = 0; i < lines.size(); i += 2) {
			String[] ss = lines.get( i ).split( " " );
			emails.add( ss[0] );
		}
		emails.removeAll( sentSetist );//移除已经完成的
		final int total = emails.size();
		int batch = 3;
		ExecutorService es = Executors.newFixedThreadPool( batch );
		final LinkedBlockingQueue<Step2Worker> cfgQ = new LinkedBlockingQueue<Step2Worker>( batch );
		for (int i = 0; i < batch; ++i) {
			//HC hc = HCs.makeHC( "202.120.17.158", 2076, false );
			HC hc = HCs.makeHC( false );
			File vcode = new File( "vcode_" + i + ".png" );
			Step2Worker cfg = new Step2Worker( hc, vcode, es );
			cfg.downloadVCodeAsync();
			cfgQ.add( cfg );
		}
		final AtomicInteger count = new AtomicInteger( 0 );
		Scanner scanner = new Scanner( System.in );
		while (true) {
			emails.removeAll( sentSetist );//移除已经完成的
			if (emails.isEmpty())
				break;
			System.out.println( emails );
			System.out.println( "还有" + emails.size() + "个" );
			for (String email : emails) {
				Step2Worker sw = cfgQ.take();
				System.out.println( "请输入验证码:" + sw.vcode.getName() );
				String yzm = scanner.nextLine();
				sw.doAfter( email, yzm, new Step2Callback() {
					public void onResult(String content, String email) throws IOException {
						int status = -1;
						if (content.contains( "邮件已发送" ))
							status = 0;
						else if (content.contains( "验证码错误" ))
							status = 1;
						if (status == 0) {
							System.out.println( email + " 邮件已发送" + count.incrementAndGet() + "/" + total );
							FileUtils.writeStringToFile( SENT_FILE, email + "\n", true );
							sentSetist.add( email );
						} else if (status == 1) {
							System.out.println( "验证码错误" );
						} else {
							System.out.println( content );
						}
					}
				} );
				cfgQ.put( sw );
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
	public void 步骤3_海南邮箱获取哔哩哔哩的urls() throws Exception {
		int batch = 16;
		ExecutorService es = Executors.newFixedThreadPool( batch );

		List<String> urls_exists = FileUtils.readLines( URLS_FILE );
		final LinkedBlockingQueue<String> usernameAndPassword = new LinkedBlockingQueue<String>();
		List<String> lines = FileUtils.readLines( EMAILS_COOKIES_FILE );
		final Map<String, Integer> failCount = new HashMap<String, Integer>();
		for (int i = 0; i < lines.size(); i += 2) {
			String[] ss = lines.get( i ).split( " " );
			String email = ss[0];
			boolean exists = false;
			for (String url : urls_exists)
				if (url.contains( email )) {
					exists = true;
					break;
				}
			if (!exists)
				usernameAndPassword.add( lines.get( i ) );
		}

		System.out.println( "个数" + usernameAndPassword.size() );
		final List<String> urls = Collections.synchronizedList( new ArrayList<String>() );

		for (int i = 0; i < batch; ++i) {
			//启动一个工作者
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					HC hc = HCs.makeHC( false );
					while (true) {
						String uap = usernameAndPassword.poll();
						if (uap == null)
							break;
						String[] ss = uap.split( " " );
						String email = ss[0];
						String username = StringUtils.substringBefore( email, "@" );
						String password = ss[1];
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

						if (loginResult == 2) {
							req = Req.post(
									"http://mail.hainan.net/webmailhainan/passwordprotection/web/hainanMibaoResult.jsp" )
									.datas( new Params(
											"psd_question", "您最喜欢的数字是？",
											"psd_answer", "1",
											"pwd_ok_btn", "确定" ).encoding( "gb2312" ) );
							String content = hc.asString( req );
							if (!( content.contains( "您已成功设置密保" ) || content.contains( "您已设置过密保" ) )) {
								System.out.println( "密保步骤失败" );
								System.out.println( content );
								loginResult = 3;
							} else
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
								urls.add( url );
							} else {
								System.out.println( "没有找到信" );
							}
						}
						req = Req.post( "http://mail.hainan.net/webmailhainan/logout.jsp" );
						hc.consume( req );
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		FileUtils.writeLines( URLS_FILE, urls, true );
	}

	@Test
	public void 步骤3_新浪邮箱进入后再激活_策略2() throws Exception {
		int batch = 16;
		ExecutorService es = Executors.newFixedThreadPool( batch );

		List<String> urls_exists = FileUtils.readLines( URLS_FILE );
		final LinkedBlockingQueue<String> cookies = new LinkedBlockingQueue<String>();
		List<String> lines = FileUtils.readLines( EMAILS_COOKIES_FILE );
		final Map<String, Integer> failCount = new HashMap<String, Integer>();
		for (int i = 0; i < lines.size(); i += 2) {
			String[] ss = lines.get( i ).split( " " );
			String email = ss[0];
			boolean exists = false;
			for (String url : urls_exists)
				if (url.contains( email )) {
					exists = true;
					break;
				}
			if (!exists)
				cookies.add( lines.get( i + 1 ) );
		}
		System.out.println( "个数" + cookies.size() );
		final List<String> urls = Collections.synchronizedList( new ArrayList<String>() );

		for (int i = 0; i < batch; ++i) {
			//启动一个工作者
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					HC hc = HCs.makeHC();
					while (true) {
						//拿到一个cookie
						String cookie = cookies.poll();
						//没cookie 就走人
						if (cookie == null)
							break;
						try {
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
							JSONArray maillist = JSON.parseObject( content ).getJSONObject( "data" )
									.getJSONArray( "maillist" );
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
									String url = StringUtils.substringBetween( content, "href=\"",
											"\" target=\"_blank\"" );
									urls.add( url );
									System.out.println( url );
									break;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							Integer count = failCount.get( cookie );
							count = count == null ? 1 : count + 1;
							if (count < 10) {
								//出了问题就放回去
								cookies.offer( cookie );
							}
							failCount.put( cookie, count );
						}
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		FileUtils.writeLines( URLS_FILE, urls, true );
	}

	@Test
	public void 步骤3_新浪邮箱进入后再激活() throws Exception {
		//HC hc = makeHC("202.195.192.197",3128,true);
		HC hc = HCs.makeHC();
		System.out.println( hc.getAsString( "http://1212.ip138.com/ic.asp" ) );
		List<String> lines = FileUtils.readLines( new File( "emails_cookies.txt" ) );
		List<String> urls_exists = FileUtils.readLines( URLS_FILE );
		for (int i = 0; i < lines.size(); i += 2) {
			String email = lines.get( i );
			for (String url : urls_exists)
				if (url.contains( email ))
					continue;
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
					FileUtils.writeStringToFile( URLS_FILE, url + "\r\n", true );
					System.out.println( url );
					break;
				}
			}
		}
	}

	private void 激活(Proxy p, List<String> urls, Map<String, String> passwordMap) {
		HC hc = HCs.makeHC( p );
		for (Iterator<String> iter = urls.iterator(); iter.hasNext();) {
			String url = iter.next();
			url = url.replace( "https", "http" );
			String email = StringUtils.substringBetween( url, "email=", "&time=" );
			String username = StringUtils.substringBefore( email, "@" );
			String password = passwordMap.get( email );
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
				System.out.println( a + " 注册成功" );
			} else if (status == 2) {
				System.out.println( "本IP已经注册过" );
				break;
			} else if (status == 3) {
				System.out.println( "不能代理!" );
				break;
			} else {
				System.out.println( content );
				break;
			}
		}
	}

	@Test
	public void 步骤4_激活哔哩哔哩并绑定账号_策略3() throws Exception {
		List<String> urls0 = FileUtils.readLines( URLS_FILE );
		final LinkedBlockingQueue<String> urls = new LinkedBlockingQueue<String>();
		for (String url : urls0) {
			String email = StringUtils.substringBetween( url, "email=", "&time=" );
			if (dao.queryForEq( "userid", email ).isEmpty())
				urls.add( url );
		}

		final int successSize = urls.size();
		final List<String> successUrls = Collections.synchronizedList( new ArrayList<String>() );

		final Map<String, String> passwordMap = new HashMap<String, String>();
		List<String> lines = FileUtils.readLines( EMAILS_COOKIES_FILE );
		for (int i = 0; i < lines.size(); i += 2) {
			String[] ss = lines.get( i ).split( " " );
			passwordMap.put( ss[0], ss[1] );
		}
		System.out.println( "有" + urls.size() + "个账号" );
		QueryBuilder<Proxy, Integer> qb = proxyDao.queryBuilder();
		//qb.where().eq( "success", true );
		List<Proxy> proxyList = new ArrayList<Proxy>();
		//proxyList.add( null );
		proxyList.addAll( qb.query() );
		System.out.println( "有" + proxyList.size() + "个代理" );
		ExecutorService es = Executors.newFixedThreadPool( Math.min( proxyList.size(), 256 ) );
		final AtomicInteger proxyCount = new AtomicInteger( proxyList.size() );
		final AtomicInteger successCount = new AtomicInteger( 0 );
		for (final Proxy p : proxyList) {
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					//System.out.println( "现在使用 " + p );
					HC hc = HCs.makeHC( p );
					LinkedList<String> myurls = new LinkedList<String>( urls );
					Collections.shuffle( myurls );
					try {
						while (!myurls.isEmpty() && successCount.get() < successSize) {
							String url = myurls.getFirst();
							String email = StringUtils.substringBetween( url, "email=", "&time=" );
							String password = passwordMap.get( email );
							int result = 激活( hc, url, email, password );
							if (result == 0) {
								myurls.removeFirst();
								System.out.println(
										email + " 激活成功 " + successCount.incrementAndGet() + "/" + successSize );
							} else if (result == 1) {
								myurls.removeFirst();
								//System.out.println( email + " 已经被激活了 " + successCount.get() + "/" + successSize );
							} else if (result == 2) {
								break;
							} else if (result == 3) {
								break;
							} else if (result == 4) {
							} else {
								myurls.removeFirst();
								break;
							}
						}
					} finally {
						System.out.println( "还有" + proxyCount.decrementAndGet() + "个代理" );
						hc.close();
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
	}

	@Test
	public void 步骤4_激活哔哩哔哩并绑定账号_策略2() throws Exception {
		List<String> urls0 = FileUtils.readLines( URLS_FILE );
		final LinkedBlockingQueue<String> urls = new LinkedBlockingQueue<String>();
		for (String url : urls0) {
			String email = StringUtils.substringBetween( url, "email=", "&time=" );
			if (dao.queryForEq( "userid", email ).isEmpty())
				urls.add( url );
		}

		final int successSize = urls.size();
		final List<String> successUrls = Collections.synchronizedList( new ArrayList<String>() );

		final Map<String, String> passwordMap = new HashMap<String, String>();
		List<String> lines = FileUtils.readLines( EMAILS_COOKIES_FILE );
		for (int i = 0; i < lines.size(); i += 2) {
			String[] ss = lines.get( i ).split( " " );
			passwordMap.put( ss[0], ss[1] );
		}
		System.out.println( "有" + urls.size() + "个账号" );
		QueryBuilder<Proxy, Integer> qb = proxyDao.queryBuilder();
		qb.where().eq( "success", true );
		//qb.orderBy( "duration", true );
		List<Proxy> proxyList = new ArrayList<Proxy>();
		proxyList.add( null );
		proxyList.addAll( qb.query() );
		//List<Proxy> proxyList =new ArrayList<Proxy>();
		//proxyList.add( null );
		System.out.println( "有" + proxyList.size() + "个代理" );
		ExecutorService es = Executors.newFixedThreadPool( Math.min( proxyList.size(), 256 ) );
		final AtomicInteger proxyCount = new AtomicInteger( proxyList.size() );
		for (Proxy p0 : proxyList) {
			final Proxy p = p0;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					//System.out.println( "现在使用 " + p );
					HC hc = HCs.makeHC( p );
					try {
						while (true) {
							if (successUrls.size() == successSize)
								break;
							String url = urls.poll( 1, TimeUnit.SECONDS );
							if (url == null)
								continue;
							String email = StringUtils.substringBetween( url, "email=", "&time=" );
							String password = passwordMap.get( email );
							int result = 激活( hc, url, email, password );
							if (result == 0) {
								successUrls.add( url );
								System.out.println( email + " 激活成功 " + successUrls.size() + "/" + successSize );
							} else if (result == 1) {
								successUrls.add( url );
								System.out.println( email + " 已经被激活了 " + successUrls.size() + "/" + successSize );
							} else if (result == 2) {
								//System.out.println( p + " 超过次数" );
								urls.add( url );
								break;
							} else if (result == 3) {
								//System.out.println( p + " 不能代理" );
								urls.add( url );
								break;
							} else if (result == 4) {
								//url已经过期...
							} else {
								//System.out.println( p + " 未知错误, 放弃该代理." );
								urls.add( url );
								break;
							}
						}
					} finally {
						System.out.println( "还有" + proxyCount.decrementAndGet() + "个代理" );
						hc.close();
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
	}

	/**
	 * 激活成功返回0 账号已经被激活返回1 超过次数返回2 不能代理返回3 其他返回4
	 * @param hc
	 * @param url
	 * @param passwordMap
	 * @return
	 */
	private int 激活(HC hc, String url, String email, String password) {
		url = url.replace( "https", "http" );
		String username = StringUtils.substringBefore( email, "@" );
		url = url.replace( "checkMail", "mailStep2" );
		if (!dao.queryForEq( "userid", email ).isEmpty())
			return 1;
		Req req = Req.post( url ).datas( "uname", username, "userpwd", password );
		for (int i = 0; i < 4; ++i)
			try {
				String content = hc.asString( req );
				if (content.contains( "注册成功" )) {
					Account a = new Account();
					a.userid = email;
					a.password = password;
					dao.create( a );
					return 0;
				}
				if (content.contains( "已有该昵称" )) {
					Account a = new Account();
					a.userid = email;
					a.password = password;
					dao.createIfNotExists( a );
					return 1;
				}
				if (content.contains( "本IP已经注册" ))
					return 2;
				if (content.contains( "禁止使用代理进行注册" ))
					return 3;
				if (content.contains( "已过期" ))
					return 4;
			} catch (RuntimeException e) {
			}
		return 5;
	}

	@Test
	public void 步骤4_激活哔哩哔哩并绑定账号() throws Exception {
		List<String> lines = FileUtils.readLines( new File( "emails_cookies.txt" ) );
		Map<String, String> password = new HashMap<String, String>();
		for (int i = 0; i < lines.size(); i += 2) {
			String[] ss = lines.get( i ).split( " " );
			password.put( ss[0], ss[1] );
		}
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
				System.out.println( "剩余=" + urls.size() + " 使用代理 " + p );
				if (urls.isEmpty())
					break;
				if (true || p.getIp().equals( "" ))
					canGo = true;
				if (!canGo)
					continue;
				try {
					激活( p, urls, password );
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println( urls.size() );
	}

	@Test
	public void 步骤5_更新账号信息() throws InterruptedException {
		int batch = 64;
		final LinkedBlockingQueue<BilibiliService2> bslist = new LinkedBlockingQueue<BilibiliService2>( batch );
		for (int i = 0; i < batch; ++i) {
			BilibiliService2 bs = new BilibiliService2();
			bs.setProxy( "202.195.192.197", 3128 );
			bs.setAutoCookie( true );
			bs.postConstruct();
			bslist.add( bs );
		}
		List<Account> list = dao.queryForEq( "currentExp", 0 );
		System.out.println( list.size() + "个" );
		ExecutorService es = Executors.newFixedThreadPool( batch );
		final LinkedBlockingQueue<Account> accounts = new LinkedBlockingQueue<Account>();
		for (Account a0 : list) {
			final Account a = a0;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					BilibiliService2 bs = bslist.take();
					try {
						bs.clear();
						bs.login( a );
						Account aa = bs.getUserInfo();
						aa.userid = a.userid;
						aa.password = a.password;
						accounts.add( aa );
						System.out.println( aa );
					} finally {
						bslist.put( bs );
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		dao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				for (Account a : accounts)
					dao.update( a );
				return null;
			}
		} );
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
		List<String> urls = FileUtils.readLines( SENT_FILE );
		FileUtils.writeLines( SENT_FILE, new TreeSet<String>( urls ) );
	}

	@Test
	public void 随机字符串() {
		System.out.println( RandomStringUtils.random( 10, true, false ).toLowerCase() );
	}

	public void after() {
		fd.quit();
	}
}
