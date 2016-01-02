package org.xzc.bilibili.autosignin;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.api2.BilibiliService3;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

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

	public void 修复() throws Exception {
		dao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				List<Account> list = dao.queryForAll();
				Date now = new Date();
				for (Account a : list) {
					a.updateAt = now;
					dao.update( a );
				}
				return null;
			}
		} );
	}

	@Test
	public void 所有() throws Exception {
		//更新一波
		DateTime today = DateTime.now().dayOfYear().roundFloorCopy();
		UpdateBuilder<Account, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue( "count", 0 );
		ub.where().lt( "updateAt", today.toDate() );
		int count = ub.update();
		System.out.println( count );

		//自动赚积分_2( dao.queryBuilder().query() );

		QueryBuilder<Account, Integer> qb = dao.queryBuilder();
		//qb.where().lt( "count", 3 );
		自动赚积分_2( qb.query() );

	}

	@Test
	public void 新账号() throws Exception {
		自动赚积分_2( dao.queryForEq( "currentExp", 0 ) );
		//QueryBuilder<Account, Integer> qb = dao.queryBuilder();
		//qb.where().isNull( "SESSDATA" );
		//自动赚积分( qb.query() );
	}

	@Test
	public void 设置密保() throws Exception {
		QueryBuilder<Account, Integer> qb = dao.queryBuilder();
		qb.where().like( "userid", "%sina.com%" );
		List<Account> list = qb.query();
		int batch = 256;
		ExecutorService es = Executors.newFixedThreadPool( batch );
		final BilibiliService3 bs = new BilibiliService3();
		bs.setProxy( "202.195.192.197", 3128 );
		bs.setBatch( batch );
		bs.init();
		final AtomicInteger count = new AtomicInteger( 0 );
		for (final Account a : list) {
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					while (true) {
						try {
							String result = bs.updateSafeQuestion( a );
							JSONObject json = JSON.parseObject( result );
							if (json.getBooleanValue( "status" )) {
								count.incrementAndGet();
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		System.out.println( count.get() );
	}

	private void 自动赚积分_2(final List<Account> list) throws Exception {
		if (list.isEmpty())
			return;

		final int batch = 256;
		ExecutorService es = Executors.newFixedThreadPool( Math.min( batch, list.size() ) );
		final BilibiliService3 bs = new BilibiliService3();
		bs.setProxy( "202.195.192.197", 3128 );
		bs.setBatch( batch );
		bs.init();
		final AtomicInteger count = new AtomicInteger( 0 );
		final LinkedBlockingQueue<Account> accounts = new LinkedBlockingQueue<Account>();
		final Map<Integer, ExpState> expStateMap = Collections.synchronizedMap( new HashMap<Integer, ExpState>() );

		for (Account aa : list) {
			final Account a = aa;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					while (true) {
						try {
							bs.earnExps( a );
							ExpState exp = bs.getExpState( a );
							if (!exp.login) {
								bs.login2( a, exp );
							}
							bs.getUserInfo( a );//以a2的数据为标准
							a.userid = a.userid;
							a.password = a.password;
							System.out.println( a + " " + exp + " " + count.incrementAndGet() + "/" + list.size() );
							accounts.add( a );
							expStateMap.put( a.mid, exp );
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		dao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				Date now = new Date();
				for (Account a : accounts) {
					a.count = expStateMap.get( a.mid ).count;
					a.updateAt = now;
					dao.update( a );
				}
				return null;
			}
		} );
		int login = 0, video = 0, share = 0;
		int[] counts = new int[] { 0, 0, 0, 0 };
		for (Account a : accounts) {
			ExpState es2 = expStateMap.get( a.mid );
			++counts[es2.count];
			login += es2.login ? 1 : 0;
			share += es2.share ? 1 : 0;
			video += es2.video ? 1 : 0;
		}
		System.out.println( "login=" + login );
		System.out.println( "video=" + video );
		System.out.println( "share=" + share );
		System.out.println( ArrayUtils.toString( counts ) );
	}

	private void 自动赚积分(final List<Account> list) throws Exception {
		if (list.isEmpty())
			return;
		int batch = 256;
		ExecutorService es = Executors.newFixedThreadPool( Math.min( batch, list.size() ) );
		final AtomicInteger count = new AtomicInteger( 0 );
		final LinkedBlockingQueue<Account> accounts = new LinkedBlockingQueue<Account>();
		final Map<Integer, ExpState> expStateMap = Collections.synchronizedMap( new HashMap<Integer, ExpState>() );
		for (Account aa : list) {
			final Account a = aa;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					try {
						BilibiliService2 bs = new BilibiliService2();
						//bs.setProxy( "202.195.192.197", 3128 );
						//bs.setProxy( "cache.sjtu.edu.cn", 8080);
						bs.postConstruct();
						while (true) {
							try {
								bs.clear();
								bs.setDedeID( "3471617" );
								bs.login( a );
								if (!bs.isLogined()) {//登陆失败就清除它 并跳过
									a.SESSDATA = null;
									dao.update( a );
									log.info( a + " 登录失败, 请手动检查." );
									continue;
								}
								a.SESSDATA = bs.getSESSDATA();
								//已经登陆了!
								boolean result = bs.shareFirst();
								if (!result)
									System.out.println( "分享结果=" + result );
								result = bs.reportWatch();
								if (!result)
									System.out.println( "报告观看=" + result );
								bs.other();
								ExpState es = bs.getExpState();
								Account a2 = bs.getUserInfo();//以a2的数据为标准
								a2.userid = a.userid;
								a2.password = a.password;
								accounts.add( a2 );
								System.out.println( a2 + " " + es + " " + count.incrementAndGet() + "/" + list.size() );
								expStateMap.put( a2.mid, es );
								break;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} finally {
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
		int[] counts = new int[] { 0, 0, 0, 0 };
		for (Account a : accounts) {
			ExpState es2 = expStateMap.get( a.mid );
			++counts[es2.count];
		}
		System.out.println( ArrayUtils.toString( counts ) );
	}

}
