package org.xzc.bilibili.scan;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.task.CommentTask;

/**
 * 自动评论
 * @author xzchaoo
 *
 */
@Component
public class AutoCommentWokerThread extends Thread {
	@Autowired
	private BilibiliService simple;
	@Autowired
	private BilibiliService main;
	@Autowired
	private CommentService commentService;

	@Autowired
	private BilibiliDB db;

	private boolean doComment(Video v) {
		//做评论
		String msg = commentService.getComment( v );
		String result = main.comment( v.aid, msg );
		System.out.println( "尝试对aid=" + v.aid + " 评论 " + msg + ", 结果是" + result );
		if (result.contains( "禁言" )) {
			throw new RuntimeException( "竟然被禁言了, 目前没法解决." );
		}
		if (result.contains( "验证码" )) {
			try {
				System.out.println( "由于验证码错误， 睡觉60秒" );
				Thread.sleep( 60000 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//有的时候即使已经OK了, 但是实际上没有评论成功! 很可能是因为本次的msg和上次的msg完全一致
		return "OK".equals( result );
	}

	@Override
	public void run() {
		while (true) {
			try {
				//获得待评论的任务
				List<CommentTask> taskList = db.getCommentTaskList();
				System.out.println( "开始执行自动评论任务, 任务数量=" + taskList.size() );
				//全部尝试做评论
				for (CommentTask ct : taskList) {
					if (!simple.isCommentListEmpty( ct.aid )) {
						//评论已经不为空了
						System.out.println( "评论已经不为空, 放弃. " + ct.aid );
						db.markFailed( ct );
					} else if (doComment( db.getVideo( ct.aid ) )) {
						//判断一下第一是不是自己
						db.markFinished( ct );
					}
				}
			} catch (Exception e) {
				try {
					FileUtils.writeStringToFile( new File( "error.log" ), e.getMessage() + "\r\n", true );
					Thread.sleep( 20000 );
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			try {
				Thread.sleep( 10000 );//休息10秒
			} catch (InterruptedException e) {
			}
		}
	}
}
