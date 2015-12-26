package org.xzc.bilibili.scan;

import java.util.concurrent.Callable;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Result;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.task.CommentTask;

/**
 * 收藏夹消耗者
 * @author xzchaoo
 *
 */
@Component
public class FavoriteListConsumer {
	public static final int BATCH_DEFAULT = 50;
	private int batch = BATCH_DEFAULT;

	public int getBatch() {
		return batch;
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

	private static final Logger log = Logger.getLogger( FavoriteListConsumer.class );
	@Autowired
	private ScanDB db;

	@Resource(name = "simpleBilibiliService")
	private BilibiliService simpleBilibiliService;

	@Autowired
	private CommentService commentService;

	/**
	 * 开始消耗一波收藏夹 返回消耗的数量
	 * @return
	 */
	public int consume() {
		if (log.isInfoEnabled())
			log.info( "消耗收藏夹" );
		int total = 0;
		
		while (true) {
			//获取收藏夹
			final FavGetList favoriteList = simpleBilibiliService.getFavoriteListJSON( batch );
			//统计
			total += favoriteList.vlist.size();
			//插入数据库
			db.getVideoDao().callBatchTasks( new Callable<Void>() {
				public Void call() throws Exception {
					for (Video v : favoriteList.vlist) {
						db.createOrUpdate( v );
					}
					return null;
				}
			} );

			//查看是否感兴趣, 如果感兴趣的话就创建一个CommentTask保存到数据库
			for (Video v : favoriteList.vlist) {
				String msg = commentService.getComment( v );
				if (msg != null) {
					db.createIfNotExists( new CommentTask( v.aid, msg ) );
				}
			}
			
			//删除
			Result r = simpleBilibiliService.deleteFavoriteJSON( favoriteList );
			if (!r.success) {
				throw new RuntimeException( simpleBilibiliService.getAccount() + " 删除收藏夹失败, 请检查账号cookie." + r );
			}
			if (favoriteList.count == favoriteList.vlist.size())
				break;
		}
		return total;
	}
}
