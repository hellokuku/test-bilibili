package org.xzc.bilibili.autosignin;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.junit.Test;
import org.xzc.bilibili.util.HC;
import org.xzc.bilibili.util.Sign;
import org.xzc.bilibili.util.Utils;

import com.alibaba.fastjson.JSON;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * 自动登录账号一下
 * @author xzchaoo
 *
 */
public class TestAutoSignIn {
	private AccountForAutoSignIn getAccountInfo(HC hc, String access_key) {
		String infoUrl = "http://api.bilibili.com/myinfo?access_key=" + access_key;
		AccountForAutoSignIn a = JSON.parseObject( hc.getAsString( infoUrl ), AccountForAutoSignIn.class );
		a.access_key = access_key;
		return a;
	}

	private AccountForAutoSignIn addAccount(HC hc, Dao<AccountForAutoSignIn, Integer> dao, String name, String passwrod)
			throws SQLException {
		Sign sign = new Sign( "userid", name, "pwd", passwrod, "appkey", "c1b107428d337928" );
		String loginUrl = "http://api.bilibili.com/login/v2?" + sign.getResult();
		String result = hc.getAsString( loginUrl );
		String access_key = JSON.parseObject( result ).getString( "access_key" );
		AccountForAutoSignIn a = getAccountInfo( hc, access_key );
		a.updateAt = new Date();
		dao.createOrUpdate( a );
		return a;
	}

	@Test
	public void test11() throws Exception {
		Class.forName( "org.sqlite.JDBC" );
		ConnectionSource cs = new JdbcConnectionSource( "jdbc:sqlite:bilibili.db" );
		TableUtils.createTableIfNotExists( cs, AccountForAutoSignIn.class );
		final Dao<AccountForAutoSignIn, Integer> dao = DaoManager.createDao( cs, AccountForAutoSignIn.class );

		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		CloseableHttpClient chc = HttpClients.custom().setDefaultRequestConfig( rc ).build();
		final HC hc = new HC( chc );

		final List<AccountForAutoSignIn> list = dao.queryForAll();
		for (AccountForAutoSignIn a : list) {
			AccountForAutoSignIn na = getAccountInfo( hc, a.access_key );
		}
		Utils.blockUntil( "刷新签到数据", DateTime.now().plusSeconds( 30 ), 1000 );
		dao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				Date now = new Date();
				for (AccountForAutoSignIn a : list) {
					AccountForAutoSignIn na = getAccountInfo( hc, a.access_key );
					na.updateAt = now;
					dao.update( a );
					System.out.println( a );
				}
				return null;
			}
		} );
		hc.close();
		cs.close();
	}
}
