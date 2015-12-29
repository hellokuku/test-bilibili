package org.xzc.bilibili.autosignin;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;

import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * 自动登录账号一下
 * @author xzchaoo
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, AutoSignInRunner.class })
@Configuration
public class AutoSignInRunner {
	private static final Logger log = Logger.getLogger( AutoSignInRunner.class );

	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	@Test
	public void 所有() throws Exception {
		自动赚积分( dao.queryForAll() );
	}

	@Test
	public void 新账号() throws Exception {
		自动赚积分( dao.queryForEq( "currentExp", 0 ) );
	}

	private void 自动赚积分(final List<Account> list) throws Exception {
		if (list.isEmpty())
			return;
		ExecutorService es = Executors.newFixedThreadPool( Math.min( 256, list.size() ) );
		final AtomicInteger count = new AtomicInteger( 0 );
		final AtomicInteger count2 = new AtomicInteger( 0 );
		final LinkedBlockingQueue<Account> accounts = new LinkedBlockingQueue<Account>();
		for (Account aa : list) {
			final Account a = aa;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					try {
						int c2 = count2.incrementAndGet();
						BilibiliService2 bs = new BilibiliService2();
						bs.setProxy( "202.195.192.197", 3128 );
						//bs.setProxy( "cache.sjtu.edu.cn", 8080);
						bs.postConstruct();
						while (true) {
							try {
								bs.clear();
								bs.setDedeID( "3435989" );
								bs.login0( a );
								/*if (!bs.isLogined()) {//登陆失败就清除它 并跳过
									a.SESSDATA = null;
									dao.update( a );
									log.info( a + " 登录失败, 请手动检查." );
									continue;
								}*/
								//已经登陆了!
								boolean result = bs.shareFirst();
								if (!result)
									System.out.println( "分享结果=" + result );
								result = bs.reportWatch();
								if (!result)
									System.out.println( "报告观看=" + result );
								bs.other();
								Account a2 = bs.getUserInfo();//以a2的数据为标准
								a2.userid = a.userid;
								a2.password = a.password;
								accounts.add( a2 );
								System.out.println( a2 + " " + count.incrementAndGet() + "/" + list.size() );
								break;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} finally {
						count2.decrementAndGet();
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		dao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				for (Account a : accounts) {
					dao.update( a );
				}
				return null;
			}
		} );
	}

}
