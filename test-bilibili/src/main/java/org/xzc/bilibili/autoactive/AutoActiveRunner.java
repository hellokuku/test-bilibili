package org.xzc.bilibili.autoactive;

import java.util.ArrayList;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.autosignin.BatchOpeartionRunner;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.scan.BilibiliService;

import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, AutoActiveRunner.class })
@Configuration
public class AutoActiveRunner {
	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	@Test
	public void test1() throws Exception {
		List<Account> list = dao.queryForAll();
		BilibiliService2 bs = new BilibiliService2();
		bs.postConstruct();
		System.out.println( bs.getHC().getCHC() );
		for (Account a : list) {
			if (a.mid != 0)
				continue;
			bs.clear();
			boolean login = bs.login( a );
			if (!login) {
				System.out.println( a.userid + " 失败" );
			}
			RegService rs = new RegService( bs.getDedeUserID(), bs.getSESSDATA() );
			if (!rs.isOK())
				while (true) {
					System.out.println( bs.getDedeUserID() + " 开始答题" );
					rs.answer1();
					System.out.println( "通过阶段1" );
					if (rs.answer2()) {
						System.out.println( "通过阶段2" );
						break;
					} else {
						System.out.println( "阶段2失败" );
					}
				}
			System.out.println( bs.getDedeUserID() + " ok!" );
		}
	}
}
