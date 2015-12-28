package org.xzc.bilibili.config;

import java.sql.SQLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.proxy.Proxy;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

@Configuration
public class DBConfig {
	@Bean(destroyMethod = "close")
	public ConnectionSource connectionSource() throws ClassNotFoundException, SQLException {
		Class.forName( "org.sqlite.JDBC" );
		JdbcConnectionSource cs = new JdbcConnectionSource( "jdbc:sqlite:bilibili.db" );
		return cs;
	}

	@Bean
	public RuntimeExceptionDao<Account, Integer> accountDao(ConnectionSource cs)
			throws SQLException {
		TableUtils.createTableIfNotExists( cs, Account.class );
		RuntimeExceptionDao dao = new RuntimeExceptionDao( DaoManager.createDao( cs, Account.class ) );
		return dao;
	}

	@Bean
	public RuntimeExceptionDao<Proxy, Integer> proxyDao(ConnectionSource cs)
			throws SQLException {
		TableUtils.createTableIfNotExists( cs, Proxy.class );
		RuntimeExceptionDao dao = new RuntimeExceptionDao( DaoManager.createDao( cs, Proxy.class ) );
		return dao;
	}

}
