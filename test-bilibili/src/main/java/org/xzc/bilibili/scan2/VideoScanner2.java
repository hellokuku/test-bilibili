package org.xzc.bilibili.scan2;

import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.api2.BilibiliService3;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.scan.CommentService;
import org.xzc.bilibili.scan.ScanDB;
import org.xzc.bilibili.task.CommentTask;
import org.xzc.bilibili.util.Utils;

/**
 * 视频扫描器
 * @author xzchaoo
 *
 */
@Component
public class VideoScanner2 {
	private static final Logger log = Logger.getLogger( VideoScanner2.class );
	@Autowired
	private ScanDB db;

	@Autowired
	private CommentService commentService;

	public void scan() {
		BilibiliService3 bs = new BilibiliService3();
		bs.setProxy( "202.120.17.158", 2076 );
		bs.init();
		int aid = db.getMaxAid( 3521521 ) + 1;//aid起点
		if (log.isTraceEnabled())
			log.trace( "从 aid=" + aid + " 起, 开始扫描视频." );
		int count = 0;
		while (true) {
			try {
				Video video = bs.getVideo( aid );
				if (video == null) {
					for (int i = 0; i < 16; ++i) {
						video = bs.getVideo( aid + i );
						if (video != null) {
							aid = aid + i;
							break;
						}
						Utils.sleep( 500 );//睡觉一下
					}
				}
				if (video == null) {
					Utils.blockUntil( "达到边界", DateTime.now().plusSeconds( 120 ), 30000 );
					continue;
				}

				String msg = commentService.getComment( video );
				if (msg != null) {
					db.createIfNotExists( new CommentTask( aid, msg ) );
				}
				if (++count == 100) {
					count = 0;
					System.out.println( "添加视频 " + video );
				}
				video.updateAt=new Date();
				//添加到数据库
				db.add( video );

				++aid;
				Utils.sleep( 1000 );
			} catch (Exception e) {
				e.printStackTrace();
				Utils.log( e.getMessage() );
				System.out.println( "发生异常, 睡觉 30秒" );
				Utils.blockUntil( "异常", DateTime.now().plusSeconds( 30 ), 10000 );
			}
		}
	}
}
