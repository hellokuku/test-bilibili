package org.xzc.bilibili.scan2;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.api2.BilibiliService3;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.scan.CommentService;
import org.xzc.bilibili.scan.ScanDB;
import org.xzc.bilibili.task.CommentTask;
import org.xzc.bilibili.util.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
		bs.initAccount( a );
		System.out.println( a + " 初始化成功!" );
		Random r = new Random();
		int count = 0;
		while (true) {
			try {
				Date now = new Date();
				List<CommentTask> taskList = db.getCommentTaskList();//获得待评论的任务
				if (!taskList.isEmpty()) {
					if (taskList.size() > 50) {
						throw new IllegalArgumentException( "任务数量太大了!" );
					}
					for (CommentTask ct : taskList) {
						Video v = db.getVideo( ct.aid );
						String msg = ct.msg == null ? commentService.getComment( v ) : ct.msg;
						if (msg == null) {//没有提供对该视频的评论, 那么就将它标记为放弃
							System.out.println( "没有提供评论, 失败 " + v );
							ct.status = 2;
						} else {
							long beg = System.currentTimeMillis();
							String result = bs.reply( a, v.aid, msg );
							long end = System.currentTimeMillis();
							JSONObject json = JSON.parseObject( result );
							int code = json.getIntValue( "code" );
							ct.status = code == 0 ? 1 : 0;
							if (code == 0 || r.nextInt( 10 ) == 0)
								System.out.println(
										"耗时=" + ( end - beg ) + " 尝试对aid=" + v.aid + " 评论 " + msg + ", 结果是"
												+ result );
						}
						ct.updateAt = now;
					}
					db.update( taskList );
				}
				if (++count == 10) {
					count = 0;
					System.out.println( "当前的自动评论任务数量=" + taskList.size() );
				}
				if (taskList.isEmpty())
					Utils.sleep( 10000 );
				else
					Utils.sleep( 1000 );
			} catch (Exception e) {
				e.printStackTrace();
				Utils.log( e.getMessage() );
				System.out.println( "自动评论异常, 睡觉10秒" );
				Utils.sleep( 10000 );
			}
		}
	}
}
