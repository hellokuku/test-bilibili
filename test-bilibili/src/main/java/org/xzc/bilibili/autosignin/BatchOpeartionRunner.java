package org.xzc.bilibili.autosignin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, BatchOpeartionRunner.class })
@Configuration
public class BatchOpeartionRunner {
	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	private BilibiliService2 bs;

	@After
	public void after() {
	}

	@Before
	public void before() {
		bs = new BilibiliService2();
		bs.setProxy( "202.195.192.197", 3128 );
		bs.postConstruct();
	}

	@Test
	public void 批量赞() {
		int action = 0;//赞=1 取消=0
		int aid = 3448994;
		int rpid = 74108069;
		List<Account> list = dao.queryForAll();
		for (Account a : list) {
			if (a.userid.endsWith( "sina.com" )) {
				bs.clear();
				bs.login( a );
				String result = bs.action( aid, rpid, action );
				System.out.println( a.name + " " + result );
			}
		}
	}

	@Test
	public void 批量举报() throws InterruptedException {
		String content = bs.getHC().getAsString( "http://1212.ip138.com/ic.asp", "gb2312" );
		System.out.println( content );
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
							String content = bs.report( 3436845, 73715100, 3, "刷屏" );
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
