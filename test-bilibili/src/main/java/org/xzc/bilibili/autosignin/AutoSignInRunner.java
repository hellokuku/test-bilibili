package org.xzc.bilibili.autosignin;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.log4j.Logger;
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
import org.xzc.http.HC;

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
	private BilibiliService2 bs;

	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	@Before
	public void before() {
		bs = new BilibiliService2();
		//bs.setProxy( "202.195.192.197", 3128 );
		bs.postConstruct();
	}

	//@Test
	public void 添加账号() throws Exception {
		String[] userids = {
				//将账号写在这里
		};
		String password = "";//将密码写在这里
		//这了仅仅保存到数据库而已
		for (String userid : userids) {
			bs.clear();
			Account a = new Account();
			a.userid = userid;
			a.password = password;
			dao.createOrUpdate( a );
			System.out.println( a );
		}
	}

	@Test
	public void 所有() throws Exception {
		//自动赚积分( dao.queryForEq( "mid", 0 ) );
		自动赚积分( dao.queryForAll() );
	}

	private void 自动赚积分(List<Account> list) throws Exception {
		for (Account a : list) {
			bs.clear();
			bs.setDedeID( "3435989" );
			bs.login( a );
			if (!bs.isLogined()) {//登陆失败就清除它 并跳过
				a.SESSDATA = null;
				dao.update( a );
				log.info( a + " 登录失败, 请手动检查." );
				continue;
			}
			while (true) {
				try {
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
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void 步骤4_激活哔哩哔哩并绑定账号() throws Exception {
		HC hc = bs.getHC();
		List<String> urls = FileUtils.readLines( new File( "urls.txt" ) );
		for (String url : urls) {
			url = url.replace( "https", "http" );
			String email = StringUtils.substringBetween( url, "email=", "&time=" );
			String username = StringUtils.substringBefore( email, "@" );
			String password = "70862045";
			url = url.replace( "checkMail", "mailStep2" );
			HttpUriRequest req = RequestBuilder.post( url )
					.addParameter( "uname", username )
					.addParameter( "userpwd", password )
					.build();
			String content = hc.asString( req );
			boolean success = content.contains( "注册成功" );
			if (success) {
				Account a = bs.getUserInfo();
				a.userid = email;
				a.password = password;
				dao.create( a );
			}
			System.out.println( username + " " + success );
		}
	}
}
