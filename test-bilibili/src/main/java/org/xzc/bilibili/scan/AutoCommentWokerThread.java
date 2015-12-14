package org.xzc.bilibili.scan;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Result;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.task.CommentTask;

/**
 * 自动评论
 * @author xzchaoo
 *
 */
@Component
public class AutoCommentWokerThread extends Thread {
	@Resource(name = "commentHelperBilibiliService")
	private BilibiliService commentHelper;

	@Resource(name = "mainBilibiliService")
	private BilibiliService main;
	@Autowired
	private CommentService commentService;

	@Autowired
	private BilibiliDB db;

	private long lastCommentTime;//上一次的评论时间, 尽量不要太集中

	@Override
	public void run() {
		lastCommentTime = System.currentTimeMillis();
		System.out.println( "自动评论线程已经启动!" );
		while (true) {
			System.out.println( "开始执行评论任务" );
			//迭代状态 0表示一切正常 1表示本次迭代出现了验证码的问题(必须要睡觉60秒) 2禁言 3其他问题
			int iterationResult = 0;
			try {
				//获得待评论的任务
				List<CommentTask> taskList = db.getCommentTaskList();
				Map<Integer, CommentTask> ctMap = new HashMap<Integer, CommentTask>();
				System.out.println( "开始执行自动评论任务, 任务数量=" + taskList.size() );
				if (taskList.size() > 50) {
					throw new IllegalArgumentException( "任务数量太大了!" );
				}
				//将他们全部加入commentHelper的收藏夹
				for (CommentTask ct : taskList) {
					commentHelper.addFavotite( ct.aid );
					ctMap.put( ct.aid, ct );
				}
				FavGetList list = commentHelper.consumeAllFavoriteListJSON();
				db.updateBatch( list.vlist );
				for (Video v : list.vlist) {
					if (v.status == 0) {//现在已经允许评论了
						CommentTask ct = ctMap.get( v.aid );//获得对应的ct
						if (ct == null)//会么?
							continue;
						if (!commentHelper.isCommentListEmpty( v.aid )) {//评论已经不为空了, 放弃
							System.out.println( "评论已经不为空, 放弃. " + v );
							db.markFailed( new CommentTask( v.aid ) );
						} else {//评论为空, 但是不要急着评论, 尽量延后一点点时间, 免得...
							if (System.currentTimeMillis() - v.updateAt.getTime() <= 10000) {
								//小于60秒就不评论
								continue;
							}
							//比如根据Video的create时间还有ct的updateAt
							//System.out.println( v.create );create估计是投稿时间, 建议不要以它为参考

							//这两个updateAt一般相差几秒, 任意一个都可以作为参考
							//System.out.println( v.updateAt );//updateAt代表该视频的最近状态的更新时间
							//System.out.println( ct.updateAt );//updateAt代表该任务的最近状态的更新时间

							//开始评论
							String msg = commentService.getComment( v );
							if (msg == null) {//没有提供对该视频的评论, 那么就将它标记为放弃
								System.out.println( "没有提供评论, 跳过 " + v );
								//ct.status = 4;
								//db.createOrUpdate( ct );
								continue;
							}
							Result r = main.comment( v.aid, msg );
							System.out.println( "尝试对aid=" + v.aid + " 评论 " + msg + ", 结果是" + r );
							if (!r.success) {
								if (r.msg.contains( "验证码" )) {
									iterationResult = 1;
								} else if (r.msg.contains( "禁言" )) {
									iterationResult = 2;
								} else {
									iterationResult = 3;//未知的结果
								}
								db.createOrUpdate( ct );
								break;
							} else {
								//评论成功
								db.markFinished( new CommentTask( v.aid ) );
								System.out.println( "评论成功! " + v );
							}
						}
					} else {//在这里评论的话是不安全的...

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					FileUtils.writeStringToFile( new File( "error.log" ), e.getMessage() + "\r\n", true );
					Thread.sleep( 60000 );
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			try {
				String text = null;
				if (iterationResult == 0) {
					text = "正常";
				} else if (iterationResult == 1) {
					text = "出现了验证码问题";
				} else if (iterationResult == 2) {
					text = "被禁言";
				} else {
					text = "未知的结果";
				}
				System.out.println( "评论任务执行完毕. 结果是 " + text );
				if (iterationResult == 1) {
					System.out.println( "评论线程睡觉30秒" );
					Thread.sleep( 60000 );
				} else {
					System.out.println( "评论线程睡觉30秒" );
					Thread.sleep( 30000 );
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
