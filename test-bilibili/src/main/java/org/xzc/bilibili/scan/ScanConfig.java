package org.xzc.bilibili.scan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.util.Utils;

@Configuration
@Import(DBConfig.class)
@ComponentScan
public class ScanConfig {
	private String proxyHost = "202.120.17.158";
	private int port = 2076;

	@Bean(name = "simpleBilibiliService")
	public BilibiliService simpleBilibiliService() {
		Account a = new Account( "xuzhichaoxh1@163.com", Utils.PASSWORD );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		bs.login();
		return bs;
	}

	@Bean(name = "mainBilibiliService")
	public BilibiliService mainBilibiliService() {
		Account a = new Account( "duruofeixh4@163.com", Utils.PASSWORD );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		bs.login();
		return bs;
	}

	@Bean(name = "commentHelperBilibiliService")
	public BilibiliService commentHelperBilibiliService() {
		//duruofeixh2@163.com
		Account a = new Account( "duruofeixh2@163.com", Utils.PASSWORD );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		bs.login();
		return bs;
	}

	@Bean(name = "testBilibiliService")
	public BilibiliService testBilibiliService() {
		// duruofeixh3@163.com
		Account a = new Account( "duruofeixh3@163.com", Utils.PASSWORD );
		BilibiliService bs = new BilibiliService( a, proxyHost, port );
		bs.login();
		return bs;
	}
}
