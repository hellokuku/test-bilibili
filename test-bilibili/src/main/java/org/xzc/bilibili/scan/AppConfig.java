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

	private String proxyHost = "202.120.17.158";
	private int port = 2076;

	@Bean(name = "simpleBilibiliService")
	public BilibiliService simpleBilibiliService() {
		//2015-12-14 10:08 xuzhichaoxh1@163.com
		Account a = new Account( 19216452, "704fe3e6,1451314068,c4d56f5e" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		return bs;
	}

	@Bean(name = "mainBilibiliService")
	public BilibiliService mainBilibiliService() {
		//duruofeixh4@163.com
		Account a = new Account( 19539291, "73c2bd41,1451314198,b861fec0" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		return bs;
	}

	@Bean(name = "commentHelperBilibiliService")
	public BilibiliService commentHelperBilibiliService() {
		//duruofeixh2@163.com
		Account a = new Account( 19534281, "ad87375f,1451314246,d36e1145" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		return bs;
	}

	@Bean(name = "testBilibiliService")
	public BilibiliService testBilibiliService() {
		// duruofeixh3@163.com
		Account a = new Account( 19539141, "5609edf4,1451314280,439316e9" );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		return bs;
	}
}
