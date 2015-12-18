package org.xzc.bilibili.scan;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.model.Bangumi;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Result;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.task.CommentTask;
import org.xzc.bilibili.util.Utils;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
public class TestApp {

	@Resource(name = "simpleBilibiliService")
	private BilibiliService simpleBilibiliService;

	@Resource(name = "mainBilibiliService")
	private BilibiliService mainBilibiliService;

	//测试是否登陆
	@Test
	public void testIsLogin() {
		assertTrue( simpleBilibiliService.isLogin() );
		assertTrue( mainBilibiliService.isLogin() );
	}

	@Resource
	private BilibiliDB db;

	public void 批量占据评论() throws Exception {
		int from = 2007730;
		int to = from + 800;
		//一定时间内不管对任何视频连续发言超过5次就需要验证码 1分钟
		//无法对该文档发表评论! 不会对这个频率产生影响
		int aid = from;
		while (aid < to) {
			//boolean empty = simpleBilibiliService.isCommentListEmpty( aid );
			boolean empty = true;
			if (empty) {
				Result r = simpleBilibiliService.comment( aid, "路过路过路过..." + randomChar() );
				System.out.println( aid + " " + r );
				if (r.success) {
					++aid;
				} else if (r.msg.contains( "验证码" )) {
					System.out.println( "睡觉60秒" );
					Thread.sleep( 60000 );
				} else {
					System.out.println( "意外情况 " + r );
					//break;
				}
			} else {
				++aid;
			}
		}
	}

	public void 测试弹幕() {
		simpleBilibiliService.danmu();
	}

	/**
	 *4个线程不断addFavorite 线程1处理0 4 8 12 之类的aid 其他线程类似...
	 *遇到边界则该线程立即结束或者等待
	 *1个线程不断消耗收藏夹
	 *由于开了4个线程 1秒大概搞12个
	 * @throws Exception
	 */
	public void 批量获取视频状态_新策略3() throws Exception {
		int from = db.getMaxAid( 3347430 ) + 1;//aid起点
		final boolean[] stop = new boolean[] { false };
		ExecutorService es = Executors.newFixedThreadPool( 4 );
		for (int i = 0; i < 4; ++i) {
			final int tfrom = from + i;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					int aid = tfrom;
					while (!stop[0]) {
						int code = simpleBilibiliService.addFavotite( aid );
						//System.out.println( "将aid=" + aid + "加入到收藏夹 code=" + code );
						if (code == 0 || code == 11007) {
							aid += 4;
						} else if (code == -1111) {
							for (int i = 0; i < 4; ++i) {
								code = simpleBilibiliService.addFavotite( aid + i * 4 );
								//System.out.println( "将aid=" + ( aid + i * 4 ) + "加入到收藏夹 code=" + code );
								if (code == 0 || code == 11007) {
									aid = aid + i * 4 + 4;
									break;
								}
							}
						}
						if (code != 0 && code != 11007 && code != -1111) {
							throw new RuntimeException( "未知的code=" + code );
						}
						if (code == -1111) {
							System.out.print( "达到边界, 休息20秒. " );
							for (int i = 0; i < 20; ++i) {
								System.out.print( i + " " );
								Thread.sleep( 1000 );
							}
							System.out.println();
						}
					}
					return null;
				}
			} );
		}

		long beg = System.currentTimeMillis();
		int total = 0;
		while (!stop[0]) {
			total += 消耗收藏夹();
			System.out.println( "total=" + total + " 耗时=" + ( System.currentTimeMillis() - beg ) / 1000 );
			Thread.sleep( 2000 );
		}
	}

	/**
	 * 先将一批视频加入收藏夹 然后再批量获取收藏夹 开搞
	 * 大概一秒可以搞定3个视频
	 * @throws Exception
	 */
	public void 批量获取视频状态_新策略2() throws Exception {
		int batch = 50;//每次检测50个aid
		int aid = db.getMaxAid( 0 ) + 1;//aid起点
		int from = aid;
		int count = 0;
		long beg = System.currentTimeMillis();
		while (true) {
			int code = simpleBilibiliService.addFavotite( aid );
			System.out.println( "将aid=" + aid + "加入到收藏夹 code=" + code );
			if (code == 0 || code == 11007) {
				++count;
				++aid;
			} else if (code == -1111) {
				for (int i = 0; i < 20; ++i) {
					code = simpleBilibiliService.addFavotite( aid + i );
					System.out.println( "将aid=" + ( aid + i ) + "加入到收藏夹 code=" + code );
					if (code == 0 || code == 11007) {
						++count;
						aid = aid + i;
						++aid;
						break;
					}
				}
			}
			if (count == batch) {
				消耗收藏夹();
				System.out.println( ( System.currentTimeMillis() - beg ) / 1000 + "秒 total=" + ( aid - from ) );
				count = 0;
			}
			if (code != 0 && code != 11007 && code != -1111) {
				throw new RuntimeException( "未知的code=" + code );
			}
			if (code == -1111) {
				if (count > 0) {
					消耗收藏夹();
					count = 0;
				}
				System.out.print( "达到边界, 休息20秒. " );
				for (int i = 0; i < 20; ++i) {
					System.out.print( i + " " );
					Thread.sleep( 1000 );
				}
				System.out.println();
			}
		}
	}

	@Autowired
	private CommentService commentService;

	private ParsedCallback parsedCallback = new ParsedCallback() {
		public void onParsed(Video v) {
			//System.out.println( "开始评估视频" + v );
			//我们只想评论 "连载动画" 并且还没有人评论它! 这些是可以马上评论的!
			if (commentService.accept( v )) {
				CommentTask ct = new CommentTask();
				ct.aid = v.aid;
				ct.status = 0;
				db.createOrUpdate( ct );
				System.out.println( "添加任务" + v );
			}
		}
	};

	@Resource(name = "testBilibiliService")
	private BilibiliService testBilibiliService;

	@Test
	public void 测试删除() {
		FavGetList fgl = testBilibiliService.getFavoriteListJSON( 50 );
		System.out.println( fgl );
		//fgl.vlist.add( new Video( 3373841 ) );
		//fgl.vlist.add( new Video( 3373842 ) );
		System.out.println( fgl.count );
		System.out.println( fgl.vlist.size() );
		Result r = testBilibiliService.deleteFavoriteJSON( fgl );
		System.out.println( r );
		fgl = testBilibiliService.getFavoriteListJSON( 50 );
		System.out.println( fgl );
	}

	@Autowired
	private AutoCommentWokerThread acwt;

	/**
	 * 用一个线程 不断使用getVideo1方法探测视频
	 * @throws Exception
	 */
	@Test
	public void 持续跟进最新的视频() throws Exception {
		Utils.blockUntil( "持续跟进最新的视频", new DateTime( 2015, 12, 18, 2, 40 ), 60000 );
		acwt.start();
		int batch = 50;//每次检测50个aid
		int aid = db.getMaxAid( 3349048 ) + 1;//aid起点
		while (true) {
			try {
				boolean reachBoundary = false;//是否达到边界
				int count = 0;
				while (count < batch) {
					++count;
					int code = simpleBilibiliService.addFavotite( aid );//直接加入收藏夹
					if (code == 0 || code == 11007) {//成功
						//不做事
					} else if (code == -1111) {//不存在可能是遇到了边界 或者 是假的, 可能再往后几个aid又可以用了! 真是的...
						for (int i = 0; i < 16; ++i) {
							code = simpleBilibiliService.addFavotite( aid + i );
							if (code == 0 || code == 11007) {//找到一个!
								aid = aid + i;
								break;
							}
						}
						if (code == -1111) {
							reachBoundary = true;
							break;
						}
					} else {
						FileUtils.writeStringToFile( new File( "error.log" ), "", true );
						System.out.println( "出问题了, code=" + code + ", 睡觉20秒" );
						Thread.sleep( 20000 );
						continue;
					}
					++aid;
				}
				消耗收藏夹( parsedCallback );
				if (reachBoundary) {
					System.out.println( "真的达到边界了, 休息300秒,再继续" );
					Thread.sleep( 300000 );
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				FileUtils.writeStringToFile( new File( "error.log" ), ex.getMessage() + "\r\n", true );
				System.out.println( "出问题了, 睡觉20秒" );
				Thread.sleep( 20000 );
				continue;
			}
		}
	}

	/**
	 * 消耗掉收藏夹 返回消耗的视频的个数
	 * @return
	 */
	private int 消耗收藏夹() {
		return 消耗收藏夹( null );
	}

	private int 消耗收藏夹(final ParsedCallback cb) {
		System.out.println( "消耗收藏夹" );
		int batch = 50;
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
			//回调
			if (cb != null)
				for (Video v : favoriteList.vlist)
					cb.onParsed( v );
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

	private Random random = new Random();

	private char randomChar() {
		return (char) ( 'A' + random.nextInt( 26 ) );
	}

	private static Date makeDate(int month, int day, int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.set( Calendar.MONTH, month - 1 );
		c.set( Calendar.DAY_OF_MONTH, day );
		c.set( Calendar.HOUR_OF_DAY, hour );
		c.set( Calendar.MINUTE, minute );
		c.set( Calendar.SECOND, 0 );
		return c.getTime();
	}

	private static void 阻塞直到(Date d) {
		int count = 0;
		while (true) {
			Date now = new Date();
			if (now.after( d ))
				return;
			try {
				if (++count == 10) {
					System.out.println( "时间没到, 继续睡觉, 还差" + ( d.getTime() - now.getTime() ) / 1000 + "秒" );
					count = 0;
				}
				Thread.sleep( 1000 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void 测试阻塞时间() {
		阻塞直到( makeDate( 12, 9, 23, 18 ) );
		System.out.println( "ok" );
	}

	/*
	public void 抢评论() throws Exception {
		ExecutorService es = Executors.newFixedThreadPool( 3 );//4个线程
		List<Future<?>> list = new ArrayList<Future<?>>();
		List<CommentTask> taskList = new ArrayList<CommentTask>();
		List<Date> taskBlockTimeList = new ArrayList<Date>();
		taskList.add( new CommentTask( 3367069, "上周的9.5吓得不轻。" ) );
		taskList.add( new CommentTask( 3367052, "临近期末考了，求保佑。" ) );
		taskList.add( new CommentTask( 3367059, "好番就是耐看。" ) );
		taskBlockTimeList.add( makeDate( 12, 12, 3, 28 ) );
		taskBlockTimeList.add( makeDate( 12, 12, 1, 03 ) );
		taskBlockTimeList.add( makeDate( 12, 12, 2, 38 ) );
		for (int i = 0; i < taskList.size(); ++i) {
			final CommentTask ct = taskList.get( i );
			final Date blockTime = taskBlockTimeList.get( i );
			Future<?> submit = es.submit( new Runnable() {
				public void run() {
					阻塞直到( blockTime );
					while (true) {
						long beg = System.currentTimeMillis();
						String result = mainBilibiliService.comment( ct.aid, ct.msg );
						long end = System.currentTimeMillis();
						System.out.println( "对 " + ct.aid + " 进行评论 " + ct.msg + " , 结果是 " + result + " 时间="
								+ ( end - beg ) );
						if ("OK".equals( result )) {
							break;
						}
					}
				}
			} );
			list.add( submit );
		}
		for (Future<?> f : list) {
			try {
				f.get();
			} catch (Exception e) {
			}
		}
		es.shutdown();
	}
	*/
	public void 获取所有番剧信息() {
		List<Bangumi> list = simpleBilibiliService.getBangumiList();
		for (Bangumi b : list) {
			System.out.println( b.getName() );
			for (String s : b.getAids()) {
				System.out.println( s );
			}
			System.out.println();
		}
	}

	public void 获取番剧每集的aid() {
		String bid = "2744";
		Bangumi b = simpleBilibiliService.getBangumi( bid );
		System.out.println( b.getName() );
		for (int i = 0; i < b.getAids().size(); ++i) {
			System.out.println( b.getAids().get( i ) );
		}
	}

	public void 更新全部() {
		final RuntimeExceptionDao<Video, Integer> dao = db.getVideoDao();
		dao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				CloseableIterator<Video> iter = dao.iterator();
				int batch = 50;
				int count = 0;
				while (iter.hasNext()) {
					Video v = iter.next();
					simpleBilibiliService.addFavotite( v.aid );
					if (++count == batch) {
						消耗收藏夹();
						count = 0;
					}
				}
				消耗收藏夹();
				return null;
			}
		} );
	}
}
