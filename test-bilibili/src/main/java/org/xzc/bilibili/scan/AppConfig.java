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
	
	private String proxyHost="27.115.75.114";
	private int port=8080;
	@Bean(name = "simpleBilibiliService")
	public BilibiliService simpleBilibiliService() {
		//2015-12-14 10:08 xuzhichaoxh1@163.com
		Account a = new Account( 19216452, "704fe3e6,1450788690,9bc262e4" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port);
		return bs;
	}

	@Bean(name = "mainBilibiliService")
	public BilibiliService mainBilibiliService() {
		//duruofeixh1@163.com
		Account a = new Account( 19539291, "73c2bd41,1450787705,761f369a" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port);
		return bs;
	}

	@Bean(name = "commentHelperBilibiliService")
	public BilibiliService commentHelperBilibiliService() {
		//duruofeixh2@163.com
		Account a = new Account( 19534281, "ad87375f,1450611219,2e864721" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port);
		return bs;
	}

	@Bean(name = "testBilibiliService")
	public BilibiliService testBilibiliService() {
		// duruofeixh3@163.com
		Account a = new Account( 19539141, "5609edf4,1450665642,f2c0edc0" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port);
		return bs;
	}
}
