package org.xzc.bilibili.autoactive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
 * 有些情况下 会莫名其妙地失败 最好解决一下 以提高效率
 * @author xzchaoo
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, AutoActiveRunner.class })
@Configuration
public class AutoActiveRunner {
	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	@Test
	public void test2() throws Exception {
		BilibiliService2 bs = new BilibiliService2();
		bs.postConstruct();
		Account a = new Account();
		a.userid = "fnxhgydsld@sina.com";
		a.password = "70862045";
		bs.login( a );
		RegService rs = new RegService( bs.getDedeUserID(), bs.getSESSDATA() );
		rs.answer1();
		rs.answer2();
	}

	@Test
	public void 单个号激活() {
		Account a = new Account();
		a.userid = "";
		a.password = "";
		//单个号激活( a );
	}

	private void 单个号激活(Account a) {
		BilibiliService2 bs = new BilibiliService2();
		//bs.setProxy( "202.195.192.197", 3128 );
		bs.postConstruct();
		while (true) {
			bs.clear();
			if (!bs.login( a )) {
				a.SESSDATA = null;
				dao.update( a );
				System.out.println( a.userid + " 登陆失败" );
				continue;
			}
			//RegService rs = new RegService( bs.getDedeUserID(), bs.getSESSDATA() );
			RegService rs = new RegService( bs.getHC() );
			if (rs.isOK())
				return;
			while (true) {
				System.out.println( bs.getDedeUserID() + " 开始答题" );
				try {
					rs.answer1();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				System.out.println( "通过阶段1" );
				try {
					if (rs.answer2()) {
						System.out.println( "通过阶段2" );
						System.out.println( bs.getDedeUserID() + " ok!" );
						return;
					} else {
						System.out.println( "阶段2失败" );
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void test1() throws Exception {
		List<Account> list = dao.queryForEq( "currentExp", 23 );
		ExecutorService es = Executors.newFixedThreadPool( Math.min( list.size(), 50 ) );
		List<Future> futureList = new ArrayList<Future>();
		for (Account aa : list.subList( 0, 1 )) {
			final Account a = aa;
			Future f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					单个号激活( a );
					return null;
				}
			} );
			futureList.add( f );
		}
		for (

		Future f : futureList)
			f.get();
		es.shutdownNow();
	}
}
