package org.xzc.bilibili.scan;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.util.Utils;

/**
 * 视频扫描器
 * @author xzchaoo
 *
 */
@Component
public class VideoScanner {
	private static final Logger log = Logger.getLogger( VideoScanner.class );

	@Autowired
	private ScanDB db;

	@Resource(name = "simpleBilibiliService")
	private BilibiliService simpleBilibiliService;

	@Autowired
	private FavoriteListConsumer favoriteListConsumer;

	public void scan() {
		int batch = 50;//每次检测50个aid
		int aid = db.getMaxAid( 3349048 ) + 1;//aid起点
		if (log.isTraceEnabled())
			log.trace( "从 aid=" + aid + " 起, 开始扫描视频." );
		while (true) {
			try {
				boolean reachBoundary = false;//是否达到边界
				int count = 0;
				while (count < batch) {
					++count;
					int code = simpleBilibiliService.addFavotite( aid );//直接加入收藏夹
					if (code == 0 || code == 11007) {//成功
						if (log.isTraceEnabled()) {
							log.trace( "添加 aid=" + aid + " 到收藏夹成功." );
						}
						//不做事
					} else if (code == -1111) {//不存在可能是遇到了边界 或者 是假的, 可能再往后几个aid又可以用了! 真是的...
						for (int i = 0; i < 16; ++i) {
							code = simpleBilibiliService.addFavotite( aid + i );
							if (code == 0 || code == 11007) {//找到一个!
								if (log.isTraceEnabled()) {
									log.trace( "添加 aid=" + aid + " 到收藏夹成功." );
								}
								aid = aid + i;
								break;
							}
							Utils.sleep( 1000 );//睡觉一下
						}
						if (code == 0 || code == 11007) {
							//empty
						} else if (code == -1111) {
							reachBoundary = true;
							break;
						} else {
							if (log.isInfoEnabled()) {
								log.info( "出问题了, code=" + code + ", 睡觉20秒" );
								Utils.log( "添加到收藏夹的时候遇到意外的code=" + code );
							}
							Utils.sleep( 20000 );
							continue;
						}
					} else {
						if (log.isInfoEnabled()) {
							log.info( "出问题了, code=" + code + ", 睡觉20秒" );
							Utils.log( "添加到收藏夹的时候遇到意外的code=" + code );
						}
						Utils.sleep( 20000 );
						continue;
					}
					++aid;
					Utils.sleep( 1000 );
				}
				favoriteListConsumer.consume();
				if (reachBoundary) {
					if (log.isInfoEnabled())
						log.info( "真的达到边界了, 休息300秒,再继续" );
					Utils.blockUntil( "达到边界再开始 ", DateTime.now().plusSeconds( 300 ), 30000 );
				} else {//没有到达边界 但是也是睡觉一下
					Utils.blockUntil( "下一轮再开始", DateTime.now().plusSeconds( 100 ), 25000 );
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Utils.log( ex.getMessage() );
				Utils.sleep( 20000 );
			}
		}
	}
}
