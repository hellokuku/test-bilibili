package org.xzc.bilibili.autosignin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
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
		RequestBuilder rb = RequestBuilder.get( "https://api.bilibili.com/myinfo" )
				.addParameter( "access_key", access_key );
		commentParams( rb );
		Sign.signTo( rb );
		HttpUriRequest req = rb.build();
		String result = hc.asString( req );
		JSONObject level_info = JSON.parseObject( result ).getJSONObject( "level_info" );
		AccountForAutoSignIn a = JSON.parseObject( result, AccountForAutoSignIn.class );
		a.currentLevel = level_info.getIntValue( "current_level" );
		a.currentMin = level_info.getIntValue( "current_min" );
		a.currentExp = level_info.getIntValue( "current_exp" );
		a.nextExp = level_info.getIntValue( "next_exp" );
		a.access_key = access_key;
		return a;
	}

	private static void commentParams(RequestBuilder rb) {
		rb.addParameter( "platform", "android" );
		rb.addParameter( "_device", "android" );
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
		//Sign sign = new Sign( "userid", a.userid, "pwd", a.password, "appkey", Sign.appkey );
		//String loginUrl = "http://api.bilibili.com/login/v2?" + sign.getResult();
		RequestBuilder rb = RequestBuilder.get( "https://api.bilibili.com/login/v2" )
				.addParameter( "userid", a.userid )
				.addParameter( "pwd", a.password )
				.addParameter( "appkey", Sign.appkey );
		commentParams( rb );
		Sign.signTo( rb );

		HttpUriRequest req = rb.build();
		String result = hc.asString( req );
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
		chc = HttpClients.custom().addInterceptorFirst( new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				request.addHeader( "User-Agent", "Mozilla/5.0 BiliDroid/4.8.6 (bbcallen@gmail.com)" );
			}
		} ).setDefaultRequestConfig( rc ).build();
		hc = new HC( chc );
	}

	@After
	public void after() throws SQLException, IOException {
		chc.close();
	}

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

	public void add() throws SQLException {
		AccountForAutoSignIn a = new AccountForAutoSignIn();
		a.userid = "bzhxh1@sina.com";
		a.password = "a";
		String access_key = login( hc, a );
		System.out.println( access_key );
	}

	@Test
	public void test11() throws Exception {
		List<AccountForAutoSignIn> list = dao.queryForAll();
		RequestBuilder rb = null;
		for (AccountForAutoSignIn a : list) {
			String access_key = login( hc, a );
			rb = RequestBuilder.get( "https://api.bilibili.com/login/renewToken" )
					.addParameter( "access_key", access_key )
					.addParameter( "appkey", Sign.appkey );
			Sign.signTo( rb );
			String result = hc.asString( rb.build() );
			System.out.println( result );
			//if (true)
			//	continue;
			AccountForAutoSignIn a2 = getAccountInfo( hc, access_key );
			a2.userid = a.userid;
			a2.password = a.password;
			dao.update( a2 );
			System.out.println( a2 );
			rb = RequestBuilder.post( "https://api.bilibili.com/x/share/first" )
					.addParameter( "access_key", access_key )
					.setEntity( new Params( "type", 33, "id", 3342515 ).toEntity() );
			commentParams( rb );
			Sign.signTo( rb );
			HttpUriRequest req = rb.build();
			result = hc.asString( req );
			System.out.println( result );

			rb = RequestBuilder.post( "https://api.bilibili.com/x/share/add" )
					.addParameter( "access_key", access_key )
					.setEntity( new Params( "aid", 3342515 ).toEntity() );
			commentParams( rb );
			Sign.signTo( rb );
			req = rb.build();
			result = hc.asString( req );
			System.out.println( result );
		}
	}
}
