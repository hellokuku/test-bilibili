package org.xzc.bilibili.scan2;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.scan.AutoCommentWoker;
import org.xzc.bilibili.scan.ScanConfig;
import org.xzc.bilibili.scan.VideoScanner;
import org.xzc.bilibili.util.Utils;

/**
 * 在这里启动扫描视频和自动评论
 * @author xzchaoo
 *
 */
@Component
public class ScanRunner2 {
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext( ScanConfig2.class );
		Utils.blockUntil( "持续跟进最新的视频", new DateTime( 2015, 12, 18, 2, 40 ), 60000 );
		ac.getBean( ScanRunner2.class ).run();
		ac.close();
	}

	@Autowired
	private AutoCommentWoker2 acwt;

	@Autowired
	private VideoScanner2 videoScanner;

	public void run() {
		new Thread( acwt ).start();
		videoScanner.scan();
	}
}
