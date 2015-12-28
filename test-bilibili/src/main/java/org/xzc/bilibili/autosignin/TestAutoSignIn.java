package org.xzc.bilibili.autosignin;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;

import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, TestAutoSignIn.class })
@Configuration
public class TestAutoSignIn {
	private static final String FIREFOX_PATH = "D:\\Program Files (x86)\\Firefox\\Firefox.exe";

	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	@Test
	public void testAutoSignIn() throws InterruptedException, ExecutionException {
		final LinkedBlockingQueue<FirefoxDriver> fdList = new LinkedBlockingQueue<FirefoxDriver>( 8 );
		for (int i = 0; i < 8; ++i) {
			FirefoxProfile fp = new FirefoxProfile();
			FirefoxBinary fb = new FirefoxBinary( new File( FIREFOX_PATH ) );
			FirefoxDriver fd = new FirefoxDriver( fb, fp );
			fdList.add( fd );
		}
		ExecutorService es = Executors.newFixedThreadPool( 8 );
		List<Account> list = dao.queryForAll();
		List<Future> flist = new LinkedList<Future>();
		boolean canGo = false;
		for (Account aa : list) {
			if (aa.mid == 20346594) {
				canGo = true;
				System.out.println( "GO!" );
			}
			if (!canGo) {
				System.out.println( "跳过 " + aa );
				continue;
			}
			final Account a = aa;
			Future<Void> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					FirefoxDriver fd = fdList.take();
					try {
						doSignIn( a, fd );
					} finally {
						fdList.put( fd );
					}
					return null;
				}
			} );
			flist.add( f );
		}
		for (Future f : flist)
			f.get();
		for (FirefoxDriver fd : fdList)
			fd.quit();
	}

	private void doSignIn(Account a, FirefoxDriver fd) throws InterruptedException {
		fd.manage().deleteAllCookies();
		//fd.get( "http://account.bilibili.com/ajax/miniLogin/minilogin" );
		fd.get( "http://www.bilibili.com" );
		Date d = DateTime.now().plusYears( 1 ).toDate();
		fd.manage().addCookie( new Cookie( "DedeUserID", Integer.toString( a.mid ), ".bilibili.com", "/", d ) );
		fd.manage().addCookie( new Cookie( "SESSDATA", a.SESSDATA, ".bilibili.com", "/", d ) );
		//fd.findElementById( "login-username" ).sendKeys( a.userid);
		//fd.findElementById( "login-passwd" ).sendKeys( "xzc@7086204511" );
		//fd.findElementById( "login-submit" ).click();
		//new WebDriverWait( fd, 20 )
		//		.until( ExpectedConditions.urlToBe( "https://account.bilibili.com/ajax/miniLogin/redirect" ) );
		/*	Cookie c1 = fd.manage().getCookieNamed( "DedeUserID" );
			Cookie c2 = fd.manage().getCookieNamed( "SESSDATA" );
			if (c1 == null || c2 == null) {
				doSignIn( username );
				return;
			}*/

		fd.get( "http://www.bilibili.com/video/av3453946/" );
		/*
		List<WebElement> es = fd.findElementsById( "enter_link_change_btn" );
		if (!es.isEmpty())
			es.get( 0 ).click();
		
		WebElement we = fd.findElementByCssSelector( "ul.top-list .v-item" );
		Actions ac = new Actions( fd );
		ac.moveToElement( we );
		Thread.sleep( 1000 );
		ac.click();
		Set<String> handlers = fd.getWindowHandles();
		ac.perform();
		Set<String> handlers2 = fd.getWindowHandles();
		handlers2.removeAll( handlers );
		fd.switchTo().window( handlers2.iterator().next() );
		WebElement share = fd.findElementByCssSelector( "div.block.share" );
		a=new Actions( fd );
		a.moveToElement( share );
		fd.findElementByCssSelector( "" );
		a.perform();
		a.moveToElement( share );*/

		//fd.close();
		//fd.switchTo().window( handlers.iterator().next() );
		System.out.println( a + " 完毕." );
	}

}
