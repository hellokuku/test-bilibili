package org.xzc.bilibili.autosignin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

	private void 自动赚积分(List<Account> list) throws Exception {
		if (list.isEmpty())
			return;
		ExecutorService es = Executors.newFixedThreadPool( Math.min( 256, list.size() ) );
		List<Future> futureList = new ArrayList<Future>();
		for (Account aa : list) {
			final Account a = aa;
			Future<Void> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					BilibiliService2 bs = new BilibiliService2();
					bs.setProxy( "202.195.192.197", 3128 );
					bs.postConstruct();
					while (true) {
						try {
							bs.clear();
							bs.setDedeID( "3435989" );
							bs.login( a );
							if (!bs.isLogined()) {//登陆失败就清除它 并跳过
								a.SESSDATA = null;
								dao.update( a );
								log.info( a + " 登录失败, 请手动检查." );
								continue;
							}
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
							dao.update( a2 );
							System.out.println( a2 );
							break;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			} );
			futureList.add( f );
		}
		for (Future f : futureList)
			f.get();
		es.shutdownNow();
	}

}
