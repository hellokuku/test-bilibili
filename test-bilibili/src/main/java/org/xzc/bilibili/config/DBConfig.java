package org.xzc.bilibili.config;

import java.sql.SQLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xzc.bilibili.autosignin.AccountForAutoSignIn;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

@Configuration
public class DBConfig {
	@Bean
	public ConnectionSource connectionSource() throws ClassNotFoundException, SQLException {
		Class.forName( "org.sqlite.JDBC" );
		JdbcConnectionSource cs = new JdbcConnectionSource( "jdbc:sqlite:bilibili.db" );
		return cs;
	}

	@Bean
	public RuntimeExceptionDao<AccountForAutoSignIn, Integer> AccountForAutoSignInDao(ConnectionSource cs)
			throws SQLException {
		TableUtils.createTableIfNotExists( cs, AccountForAutoSignIn.class );
		RuntimeExceptionDao dao = new RuntimeExceptionDao( DaoManager.createDao( cs, AccountForAutoSignIn.class ) );
		return dao;
	}

}
