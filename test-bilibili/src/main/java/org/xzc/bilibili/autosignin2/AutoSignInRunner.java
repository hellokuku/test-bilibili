package org.xzc.bilibili.autosignin2;

import java.util.List;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.autosignin.AccountForAutoSignIn;
import org.xzc.bilibili.config.DBConfig;

import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@ContextConfiguration(classes = { AutoSignInRunner.class, DBConfig.class })
public class AutoSignInRunner {
	private static final Logger log = Logger.getLogger( AutoSignInRunner.class );
	@Autowired
	private BilibiliService2 bs;

	@Autowired
	RuntimeExceptionDao<AccountForAutoSignIn, Integer> dao;

	@Bean
	public BilibiliService2 bs() {
		return new BilibiliService2();
	}

	@Test
	public void test1() throws Exception {
		List<AccountForAutoSignIn> queryForAll = dao.queryForAll();
		for (AccountForAutoSignIn a : queryForAll) {
			if (a.SESSDATA != null) {
				bs.setDedeUserID( Integer.toString( a.mid ) );
				bs.setSESSDATA( a.SESSDATA );
			} else {
				bs.login( a.userid, a.password );
				a.mid = Integer.parseInt( bs.getDedeUserID() );
				a.SESSDATA = bs.getSESSDATA();
			}
			if (!bs.isLogined()) {
				a.SESSDATA = null;
				dao.update( a );
				log.info( a + " 登录失败, 请手动检查." );
				continue;
			}

			//已经登陆了!
			boolean result = bs.shareFirst( 45229 );
			System.out.println( "分享结果=" + result );
			result = bs.reportWatch();
			System.out.println( "报告观看=" + result );
			AccountForAutoSignIn a2 = bs.getUserInfo();
			System.out.println( a2 );
			a.currentLevel = a2.currentLevel;
			a.currentMin = a2.currentMin;
			a.currentExp = a2.currentExp;
			a.nextExp = a2.nextExp;
			dao.update( a );
		}
	}
}
