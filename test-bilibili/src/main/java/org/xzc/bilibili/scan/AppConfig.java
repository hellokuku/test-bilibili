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
		//2015-12-14 10:08
		Account a = new Account( 19216452, "704fe3e6,1450663497,f2f2cdd9" );
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

	@Bean(name = "testBilibiliService")
	public BilibiliService testBilibiliService() {
		// duruofeixh3@163.com
		Account a = new Account( 19539141, "5609edf4,1450665642,f2c0edc0" );
		BilibiliService bs = new BilibiliService( a );
		return bs;
	}
}
