package org.xzc.bilibili.scan;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.util.Utils;

/**
 * 在这里启动扫描视频和自动评论
 * @author xzchaoo
 *
 */
@Component
public class ScanRunner {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext( AppConfig.class );
		Utils.blockUntil( "持续跟进最新的视频", new DateTime( 2015, 12, 18, 2, 40 ), 60000 );
		ac.getBean( ScanRunner.class ).run();
		ac.close();
	}

	@Autowired
	private AutoCommentWoker acwt;

	@Autowired
	VideoScanner videoScanner;

	public void run() {
		new Thread( acwt ).start();
		videoScanner.scan();
	}
}
