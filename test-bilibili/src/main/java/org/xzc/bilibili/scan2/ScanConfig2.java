package org.xzc.bilibili.scan2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.scan.BilibiliService;
import org.xzc.bilibili.scan.CommentService;
import org.xzc.bilibili.scan.ScanDB;
import org.xzc.bilibili.util.Utils;

@Configuration
@Import(DBConfig.class)
@ComponentScan
public class ScanConfig2 {
	@Bean
	public CommentService commentService() {
		return new CommentService();
	}

	@Bean
	public ScanDB scanDB() {
		return new ScanDB();
	}
}
