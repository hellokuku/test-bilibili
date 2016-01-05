package org.xzc.sina.email.auto;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.xzc.bilibili.util.HCs;
import org.xzc.http.HC;
import org.xzc.http.Req;
import org.xzc.vcode.IWorker;
import org.xzc.vcode.PositionManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;

public class Step1Worker3 implements IWorker {
	private final String tag;
	private final ExecutorService es;
	private final Step1Callback2 cb;
	private final PositionManager pm;
	private final HC hc;

	public Step1Worker3(String tag, PositionManager pm, ExecutorService es, Step1Callback2 cb) {
		this.tag = tag;
		this.pm = pm;
		this.es = es;
		this.cb = cb;
		hc = HCs.makeHC( "202.195.192.197", 3128, false );
	}

	private void doAfter(String yzm) {
		Req req = Req.post( "http://mail.hainan.net/webmailhainan/hn_adduser.jsp" )
				.datas( "email_text", username,
						"email_type", "hainan.net",
						"pwd", password,
						"pwd_again", password,
						"phone_number", "",
						"codetext", yzm );
		String email = username + "@hainan.net";
		CloseableHttpResponse res = null;
		try {
			res = hc.asRes( req );
			int code = res.getStatusLine().getStatusCode();
			if (code == 200) {
				String content = EntityUtils.toString( res.getEntity() );
				cb.callback( email, password, "无", 2 );
			} else if (code == 302) {
				Header h = res.getFirstHeader( "Location" );
				String location = h.getValue();
				if (location.startsWith( "http://mail.hainan.net/webmailhainan/login.jsp" )) {
					System.out.println( URI.create( location ).getQuery() );
					cb.callback( email, password, "无", 3 );
				} else if (location.startsWith( "http://mail.hainan.net/webmailhainan/login_submit.jsp" )) {
					cb.callback( email, password, "无", 0 );
				} else {
					cb.callback( email, password, "无", 3 );
				}
			} else {
				cb.callback( email, password, "无", 3 );
			}
		} catch (Exception e) {
			try {
				cb.callback( email, password, "无", 3 );
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			HttpClientUtils.closeQuietly( res );
		}
		initAsync();
	}

	public void doAfterAsync(final String yzm) {
		es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				doAfter( yzm );
				return null;
			}
		} );
	}

	public String getTag() {
		return tag;
	}

	public byte[] getVCodeData() {
		return vcodeData;
	}

	private static final String regmailUrl = "https://mail.sina.com.cn/register/regmail.php";
	private String username;
	private String password;
	private static Method _getElementp;

	private static final DomElement getElement(HtmlUnitWebElement e) {
		try {
			if (_getElementp == null) {
				_getElementp = e.getClass().getDeclaredMethod( "getElement" );
				_getElementp.setAccessible( true );
			}
			DomElement de = (DomElement) _getElementp.invoke( e );
			return de;
		} catch (Exception ex) {
			throw new RuntimeException( ex );
		}
	}

	private void init0() throws IOException {
		username = RandomStringUtils.random( 10, true, false ).toLowerCase();
		password = RandomStringUtils.random( 10, true, false ).toLowerCase();

		Req req = Req.get( "http://mail.hainan.net/webmailhainan/tianyaValidateCode.jsp" )
				.header( "Referer", "http://mail.hainan.net/webmailhainan/register.jsp" );
		vcodeData = hc.asByteArray( req );
		pm.bind( this );
	}

	private byte[] vcodeData;

	public void initAsync() {
		es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				init0();
				return null;
			}
		} );
	}

	public void onReject() {
	}

	public void process(String ptag) {
		System.out.println( "请输入验证码 " + ptag );
		Scanner scanner = new Scanner( System.in );
		String yzm = scanner.nextLine();
		doAfterAsync( yzm );
		scanner = null;
	}

}
