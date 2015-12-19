package org.xzc.bilibili.autosignin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api.Params;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.util.HC;
import org.xzc.bilibili.util.Sign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;

/**
 * 自动登录账号一下
 * @author xzchaoo
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DBConfig.class)
public class TestAutoSignIn {
	/**
	 * 获得账号信息 
	 * @param hc
	 * @param access_key
	 * @return
	 */
	private AccountForAutoSignIn getAccountInfo(HC hc, String access_key) {
		if (access_key == null)
			throw new IllegalArgumentException( "access_key不能为null." );
		String infoUrl = "http://api.bilibili.com/myinfo?access_key=" + access_key;
		String result = hc.getAsString( infoUrl );
		JSONObject level_info = JSON.parseObject( result ).getJSONObject( "level_info" );
		AccountForAutoSignIn a = JSON.parseObject( result, AccountForAutoSignIn.class );
		a.currentLevel = level_info.getIntValue( "current_level" );
		a.currentMin = level_info.getIntValue( "current_min" );
		a.currentExp = level_info.getIntValue( "current_exp" );
		a.nextExp = level_info.getIntValue( "next_exp" );
		a.access_key = access_key;
		return a;
	}

	/**
	 * 利用账号信息进行登录, 返回access_key
	 * @param hc
	 * @param a
	 * @return
	 * @throws SQLException
	 */
	private String login(HC hc, AccountForAutoSignIn a)
			throws SQLException {
		Sign sign = new Sign( "userid", a.userid, "pwd", a.password, "appkey", "c1b107428d337928" );
		String loginUrl = "http://api.bilibili.com/login/v2?" + sign.getResult();
		String result = hc.getAsString( loginUrl );
		JSONObject json = JSON.parseObject( result );
		int code = json.getIntValue( "code" );
		if (code != 0) {
			throw new RuntimeException( json.getString( "message" ) );
		}
		String access_key = JSON.parseObject( result ).getString( "access_key" );
		return access_key;
	}

	private CloseableHttpClient chc;
	private HC hc;
	@Autowired
	private RuntimeExceptionDao<AccountForAutoSignIn, Integer> dao;

	@Before
	public void before() throws ClassNotFoundException, SQLException {
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		chc = HttpClients.custom().setDefaultRequestConfig( rc ).build();
		hc = new HC( chc );
	}

	@After
	public void after() throws SQLException, IOException {
		chc.close();
	}

	@Test
	public void 添加新账号() throws SQLException {
		AccountForAutoSignIn a = new AccountForAutoSignIn();
		a.userid = "";
		a.password = "";
		String access_key = login( hc, a );
		AccountForAutoSignIn a2 = getAccountInfo( hc, access_key );
		a2.userid = a.userid;
		a2.password = a.password;
		dao.createOrUpdate( a2 );
	}

	@Test
	public void test11() throws Exception {
		List<AccountForAutoSignIn> list = dao.queryForAll();
		for (AccountForAutoSignIn a : list) {
			String access_key = login( hc, a );

			AccountForAutoSignIn a2 = getAccountInfo( hc, access_key );
			a2.userid = a.userid;
			a2.password = a.password;
			dao.update( a2 );
			System.out.println( a2 );
			String result = hc.postAsString( "http://api.bilibili.com/x/share/first",
					new Params( "access_key", access_key ), new Params( "type", 20, "id", 3406584 ),
					true );
			System.out.println( result );
		}
	}
}
