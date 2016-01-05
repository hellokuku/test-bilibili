package org.xzc.bilibili.scan2;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.api2.BilibiliService3;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.scan.CommentService;
import org.xzc.bilibili.scan.ScanDB;
import org.xzc.bilibili.task.CommentTask;
import org.xzc.bilibili.util.Utils;

/**
 * 自动评论
 * @author xzchaoo
 *
 */
@Component
public class AutoCommentWoker2 implements Runnable {

	@Autowired
	private CommentService commentService;

	@Autowired
	private ScanDB db;

	public void run() {
		Account a = new Account( "duruofeixh4@163.com", Utils.PASSWORD );
		BilibiliService3 bs = new BilibiliService3();
		bs.setProxy( "202.120.17.158", 2076 );
		bs.init();
		if (!bs.login( a )) {
			throw new IllegalStateException( "未登录! " + a );
		} else {
			System.out.println( a + " 初始化成功!" );
		}

		int count = 0;
		while (true) {
			try {
				List<CommentTask> taskList = db.getCommentTaskList();//获得待评论的任务
				if (!taskList.isEmpty()) {
					if (taskList.size() > 50) {
						throw new IllegalArgumentException( "任务数量太大了!" );
					}

					for (CommentTask ct : taskList) {
						Video v = bs.getVideo( ct.aid );
						if (v == null) {
							System.out.println( "找不到视频!" + ct );
							continue;
						}
						if (v.status != 0)
							continue;
						if (!bs.isCommentEmpty( v.aid )) {
							System.out.println( "评论已经不为空, 放弃. " + v );
							db.markFailed( new CommentTask( v.aid ) );
						} else {
							String msg = commentService.getComment( v );
							if (msg == null) {//没有提供对该视频的评论, 那么就将它标记为放弃
								System.out.println( "没有提供评论, 失败 " + v );
								db.markFailed( new CommentTask( v.aid ) );
							} else {
								String result = bs.reply( a, v.aid, msg );
								System.out.println( "尝试对aid=" + v.aid + " 评论 " + msg + ", 结果是" + result );
								db.markFinished( new CommentTask( v.aid ) );
							}
						}
					}
				}
				if (++count == 10) {
					count = 0;
					System.out.println( "当前的自动评论任务数量="+taskList.size() );
				}
				Utils.sleep( 5000 );
			} catch (Exception e) {
				e.printStackTrace();
				Utils.log( e.getMessage() );
				System.out.println( "自动评论异常, 睡觉10秒" );
				Utils.sleep( 10000 );
			}
		}
	}
}
