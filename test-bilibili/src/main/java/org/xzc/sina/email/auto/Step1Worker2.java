package org.xzc.sina.email.auto;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.xzc.vcode.IWorker;
import org.xzc.vcode.PositionManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;

public class Step1Worker2 implements IWorker {
	private final HtmlUnitDriver d;
	private final String tag;
	private final ExecutorService es;
	private final Step1Callback2 cb;
	private final PositionManager pm;

	public Step1Worker2(String tag, PositionManager pm, ExecutorService es, Step1Callback2 cb) {
		this.tag = tag;
		this.pm = pm;
		this.es = es;
		this.cb = cb;
		d = new HtmlUnitDriver( BrowserVersion.FIREFOX_38, true );
	}

	private static final int TIMEOUT = 1800;

	private void doAfter(String yzm) {
		try {
			//填写验证码 并提交
			d.findElementByName( "imgvcode" ).clear();
			d.findElementByName( "imgvcode" ).sendKeys( yzm );
			d.findElementByCssSelector( ".subIco" ).click();

			//等大提交结果
			int status = -1;
			for (int j = 0; j < TIMEOUT; ++j) {
				String url = d.getCurrentUrl();
				if (url.contains( "m0.mail.sina.com.cn/classic/index.php" )) {
					status = 0;
					break;
				} else if (url.contains( "mail.sina.com.cn/register/regmail.php" )) {
					List<WebElement> es = d.findElementsByCssSelector( ".syserror_alp" );
					if (!es.isEmpty()) {
						if (es.get( 0 ).getText().contains( "注册疲劳" )) {
							status = 1;
							break;
						}
					}
					es = d.findElementsByCssSelector( ".tipInfor > abbr" );
					if (!es.isEmpty()) {
						WebElement we = es.get( es.size() - 1 );
						if (we.getText().contains( "输入错误" )) {
							status = 2;
							break;
						}
					}
				}
				Thread.sleep( 100 );
			}
			cb.callback( username + "@sina.com", password, buildCookie( d.manage().getCookies() ), status );
		} catch (Exception e) {
		} finally {
			initAsync();
		}

	}

	private static String buildCookie(Set<Cookie> set) {
		StringBuilder sb = new StringBuilder();
		for (Cookie c : set) {
			sb.append( c.getName() + "=" + c.getValue() + ";" );
		}
		return sb.toString();
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
		d.manage().deleteAllCookies();
		d.get( regmailUrl );
		//生成账号密码
		username = RandomStringUtils.random( 10, true, false ).toLowerCase();
		password = RandomStringUtils.random( 10, true, false ).toLowerCase();
		//填写账号密码
		d.findElementByName( "email" ).sendKeys( username );
		d.findElementByName( "psw" ).sendKeys( password );
		//将图片保存一下
		HtmlUnitWebElement yzm = (HtmlUnitWebElement) d.findElementById( "capcha" );
		HtmlImage hi = (HtmlImage) getElement( yzm );
		File tempFile = File.createTempFile( "vcode", null );
		hi.saveAs( tempFile );
		vcodeData = FileUtils.readFileToByteArray( tempFile );
		tempFile.delete();
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
