package org.xzc.bilibili.autosignin;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;

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
	public void 批量举报() {
		String content = bs.getHC().getAsString( "http://1212.ip138.com/ic.asp", "gb2312" );
		System.out.println( content );
		List<Account> list = dao.queryForAll();
		for (Account a : list) {
			if (a.userid.endsWith( "sina.com" )) {
				bs.clear();
				bs.login( a);
				String result = bs.report( 3448994, 74108069, 3, "刷屏" );
				System.out.println( a + " " + result );
			}
		}
	}
}
