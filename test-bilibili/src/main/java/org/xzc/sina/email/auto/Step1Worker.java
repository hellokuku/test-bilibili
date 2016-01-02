package org.xzc.sina.email.auto;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;

public class Step1Worker {
	public final HtmlUnitDriver d;
	public final File vcode;
	private final ExecutorService es;
	public final Object lock = new Object();

	public Step1Worker(HtmlUnitDriver d, File vcode, ExecutorService es) {
		this.d = d;
		this.vcode = vcode;
		this.es = es;
	}

	private static Method _getElementp;

	private static final DomElement getElement(HtmlUnitWebElement e) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (_getElementp == null) {
			_getElementp = e.getClass().getDeclaredMethod( "getElement" );
			_getElementp.setAccessible( true );
		}
		DomElement de = (DomElement) _getElementp.invoke( e );
		return de;
	}

	private static final String regmailUrl = "https://mail.sina.com.cn/register/regmail.php";

	public String username;
	public String password;

	private void init0() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
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
		hi.saveAs( vcode );
	}

	public void init() {
		es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				synchronized (lock) {
					init0();
				}
				return null;
			}
		} );
	}

	private static final int TIMEOUT = 300;

	public void doAfter(final String vcode, final Step1Callback cb) {
		es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				synchronized (lock) {
					try {
						//填写验证码 并提交
						d.findElementByName( "imgvcode" ).clear();
						d.findElementByName( "imgvcode" ).sendKeys( vcode );
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
						cb.onResult( Step1Worker.this, status );
					} finally {
						init0();
					}
				}
				return null;
			}
		} );
	}

}
