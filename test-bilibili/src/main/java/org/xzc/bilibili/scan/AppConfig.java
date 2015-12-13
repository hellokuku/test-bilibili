package org.xzc.bilibili.scan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.xzc.bilibili.model.Account;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

@Configuration
@ComponentScan
public class AppConfig {

	@Bean(destroyMethod = "close")
	public ConnectionSource connectionSource() {
		try {
			Class.forName( "org.sqlite.JDBC" );
			ConnectionSource cs = new JdbcConnectionSource( "jdbc:sqlite:bilibili.db" );
			return cs;
		} catch (Exception e) {
			throw new IllegalStateException( e );
		}
	}

	@Bean(name = "simpleBilibiliService")
	public BilibiliService simpleBilibiliService() {
		Account a = new Account( 19216452, "704fe3e6,1450055207,1cc44621" );
		BilibiliService bs = new BilibiliService( a );
		return bs;
	}

	@Bean(name = "mainBilibiliService")
	public BilibiliService mainBilibiliService() {
		Account a = new Account( 19533545, "f0ee7f17,1450611284,7a892989" );
		BilibiliService bs = new BilibiliService( a );
		return bs;
	}

	@Bean(name = "commentHelperBilibiliService")
	public BilibiliService commentHelperBilibiliService() {
		Account a = new Account( 19534281, "ad87375f,1450611219,2e864721" );
		BilibiliService bs = new BilibiliService( a );
		return bs;
	}
}
