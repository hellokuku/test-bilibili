package org.xzc.bilibili.autosignin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;
import org.xzc.http.HC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, BatchOpeartionRunner.class })
@Configuration
public class BatchOpeartionRunner {
	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	@Before
	public void before() {
	}

	@After
	public void after() {
	}

	@Test
	public void 批量赞() throws InterruptedException, ExecutionException {
		final int action = 0;//赞=1 取消=0
		final int aid = 3462980;
		final int rpid = 74437874;

		List<Account> list = dao.queryForAll();
		ExecutorService es = Executors.newFixedThreadPool( Math.min( list.size(), 256 ) );
		List<Future<Integer>> flist = new LinkedList<Future<Integer>>();
		int c = 0;
		for (Account aa : list) {
			if (++c == 20)
				break;
			final Account a = aa;
			Future<Integer> f = es.submit( new Callable<Integer>() {
				public Integer call() throws Exception {
					BilibiliService2 bs = new BilibiliService2();
					bs.setProxy( "202.195.192.197", 3128 );
					bs.postConstruct();
					bs.login0( a );
					String content = bs.action( aid, rpid, action );
					JSONObject json = JSON.parseObject( content );
					return json.getIntValue( "code" );
				}
			} );
			flist.add( f );
		}
		int count = 0;
		for (Future<Integer> f : flist)
			if (f.get() == 0)
				count += 1;
		es.shutdown();
		System.out.println( count );
	}

	@Test
	public void 批量举报() throws InterruptedException {
		final String[] types = new String[] { "广告", "色情", "刷屏", "引战", "剧透", "政治", "人身攻击", "视频不相关" };
		List<Account> list = dao.queryForAll();
		Iterator<Account> it = list.iterator();
		while (it.hasNext()) {
			Account a = it.next();
			if (!a.userid.endsWith( "sina.com" )) {
				it.remove();
			}
		}
		ExecutorService es = Executors.newFixedThreadPool( list.size() );

		final AtomicBoolean stop = new AtomicBoolean( false );
		final AtomicInteger count = new AtomicInteger( 0 );
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		final Random r = new Random();
		for (Account account : list) {
			final Account a = account;
			Future<Void> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					BilibiliService2 bs = new BilibiliService2();
					bs.setProxy( "202.195.192.197", 3128 );
					bs.postConstruct();
					bs.login( a );
					while (!stop.get()) {
						try {
							int type = r.nextInt( types.length );
							String content = bs.report( 3420311, 73244103, type + 1, types[type] );
							JSONObject json = JSON.parseObject( content );
							int code = json.getIntValue( "code" );
							if (code == 0) {
								int c = count.incrementAndGet();
								System.out.println( c );
							} else if (code == 12019) {
								int ttl = json.getJSONObject( "data" ).getIntValue( "ttl" );
								Thread.sleep( ttl * 1000 );
							} else if (code == 12005) {
								System.out.println( DateTime.now() );
								stop.set( true );
							} else {
								System.out.println( content );
							}

						} catch (RuntimeException e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			} );
			futureList.add( f );
		}
		Runtime.getRuntime().addShutdownHook( new Thread() {
			public void run() {
				System.out.println( "count=" + count.get() );
			}
		} );
		for (Future f : futureList)
			try {
				f.get();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		es.shutdown();
	}
}
