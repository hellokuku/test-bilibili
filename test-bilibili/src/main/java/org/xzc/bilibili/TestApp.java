package org.xzc.bilibili;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.dao.BilibiliDB;
import org.xzc.bilibili.model.Bangumi;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.model.json.FavGetList;
import org.xzc.bilibili.task.CommentTask;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class })
public class TestApp {
	@Autowired
	@Qualifier("simpleBilibiliService")
	private BilibiliService simpleBilibiliService;
	@Autowired
	@Qualifier("mainBilibiliService")
	private BilibiliService mainBilibiliService;

	//测试是否登陆
	@Test
	public void testIsLogin() {
		assertTrue( simpleBilibiliService.isLogin() );
		assertTrue( mainBilibiliService.isLogin() );
	}

	@Test
	public void 获取视频信息0() {
		int aid = 3334306;
		Video v = simpleBilibiliService.getVideo0( aid );
		System.out.println( v );
	}

	@Test
	public void 获取视频信息1() {
		int aid = 3334603;
		Video v = simpleBilibiliService.getVideo1( aid );
		System.out.println( v );
	}

	@Test
	public void 获得收藏夹内容_JSON() {
		FavGetList json = simpleBilibiliService.getFavoriteListJSON( 10 );
		System.out.println( json.count );
	}

	@Test
	public void 删除收藏夹内容_JSON() {
		FavGetList json = simpleBilibiliService.getFavoriteListJSON( 10 );
		System.out.println( json.count );
		simpleBilibiliService.deleteFavoriteJSON( json );
		json = simpleBilibiliService.getFavoriteListJSON( 10 );
		System.out.println( json.count );
	}

	@Autowired
	private BilibiliDB db;

	@Test
	public void 测试评论间隔() throws Exception {
		int aid = 2007730;
		while (true) {
			String result = simpleBilibiliService.comment( aid, "路过路过路过..." + randomChar() );
			System.out.println( result );
			for (int i = 0; i < 60; ++i) {
				System.out.print( i + " " );
				Thread.sleep( 1000 );
			}
			System.out.println();
		}
	}

	@Test
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
				String result = simpleBilibiliService.comment( aid, "路过路过路过..." + randomChar() );
				System.out.println( aid + " " + result );
				if ("OK".equals( result )) {
					++aid;
				} else if (result.contains( "验证码" )) {
					System.out.println( "睡觉60秒" );
					Thread.sleep( 60000 );
				} else {
					System.out.println( "意外情况 " + result );
					//break;
				}
			} else {
				++aid;
			}
		}
	}

	@Test
	public void 测试弹幕() {
		simpleBilibiliService.danmu();
	}

	private Video 处理伪边界(int aid) {
		for (int i = 0; i < 16; ++i) {
			Video v = simpleBilibiliService.getVideo1( aid + i );
			if (!v.notExists()) {
				return v;
			}
		}
		return null;
	}

	private void 更新视频状态(int aid) {
		Video v = simpleBilibiliService.getVideo1( aid );
		if (!v.notExists()) {
			db.createOrUpdate( v );
			if (v.isMQX()) {
				simpleBilibiliService.addFavotite( aid );//加入收藏夹
			}
		}
		System.out.println( "添加" + v );
	}

	/**
	 *4个线程不断addFavorite 线程1处理0 4 8 12 之类的aid 其他线程类似...
	 *遇到边界则该线程立即结束或者等待
	 *1个线程不断消耗收藏夹
	 *由于开了4个线程 1秒大概搞12个
	 * @throws Exception
	 */
	@Test
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
	@Test
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

	private boolean doComment(Video v) {
		//做评论
		String msg = commentService.getComment( v );
		String result = mainBilibiliService.comment( v.aid, msg );
		System.out.println( "0尝试对aid=" + v.aid + " 评论 " + msg + ", 结果是" + result );
		if (result.contains( "禁言" )) {
			mainBilibiliService.rebuildContext();
		}
		if (result.contains( "验证码" )) {//验证码 睡觉18秒
			try {
				System.out.println( "由于验证码错误， 睡觉20秒" );
				Thread.sleep( 20000 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//有的时候即使已经OK了, 但是实际上没有评论成功!
		return "OK".equals( result );
	}

	private ParsedCallback parsedCallback = new ParsedCallback() {
		public void onParsed(Video v) {
			//System.out.println( "开始评估视频" + v );
			//我们只想评论 "连载动画" 并且还没有人评论它! 这些是可以马上评论的!
			if (commentService.accept( v )) {
				CommentTask ct = new CommentTask();
				ct.aid = v.aid;
				ct.status = 0;
				db.addOrUpdateCommentTask( ct );
			}
		}
	};

	/**
	 * 用一个线程 不断使用getVideo1方法探测视频
	 * @throws Exception
	 */
	@Test
	public void 持续跟进最新的视频() throws Exception {
		//启动一个线程不断扫描任务
		db.fixCommentTask();
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						List<CommentTask> taskList = db.getCommentTaskList();
						System.out.println( "开始执行自动评论任务, 任务数量=" + taskList.size() );
						//评论!
						for (CommentTask ct : taskList) {
							if (!simpleBilibiliService.isCommentListEmpty( ct.aid )) {
								//评论已经不为空了
								System.out.println( "评论已经不为空, 放弃. " + ct.aid );
								db.markFailed( ct );
							} else if (doComment( db.getVideo( ct.aid ) )) {
								//判断一下第一是不是自己
								db.markFinished( ct );
							}
						}
					} catch (RuntimeException e) {
						simpleBilibiliService.rebuildContext();
						mainBilibiliService.rebuildContext();
						try {
							FileUtils.writeStringToFile( new File( "error.log" ), e.getMessage() + "\r\n", true );
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						try {
							Thread.sleep( 20000 );
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					try {
						Thread.sleep( 5000 );//休息5秒
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();

		int batch = 10;//每次检测50个aid
		boolean reachBoundary = false;//是否达到边界
		int aid = db.getMaxAid( 3349048 ) + 1;//aid起点
		List<Integer> successAidList = new ArrayList<Integer>();
		while (true) {
			try {
				int count = 0;
				while (count < batch) {
					++count;
					int code = simpleBilibiliService.addFavotite( aid );//直接加入收藏夹
					if (code == 0 || code == 11007) {//成功
						successAidList.add( aid );
					} else if (code == -1111) {//不存在可能是遇到了边界 或者 是假的, 可能再往后几个aid又可以用了! 真是的...
						for (int i = 0; i < 16; ++i) {
							code = simpleBilibiliService.addFavotite( aid + i );
							if (code == 0 || code == 11007) {//找到一个!
								aid = aid + i;
								successAidList.add( aid );
								break;
							}
						}
						if (code == -1111) {
							reachBoundary = true;
							break;
						}
					} else if (code == -101) {
						//{"code":-101,"message":"Account is not logined.","ts":1449589626}
						simpleBilibiliService.rebuildContext();
						mainBilibiliService.rebuildContext();
						System.out.println( "出问题了code=-101, 睡觉20秒" );
						Thread.sleep( 20000 );
						continue;
					} else {
						String content = simpleBilibiliService.getLastFavoriteContent();
						FileUtils.writeStringToFile( new File( "error.log" ), content + "\r\n", true );
						simpleBilibiliService.rebuildContext();
						mainBilibiliService.rebuildContext();
						System.out.println( "出问题了, 睡觉20秒" );
						Thread.sleep( 20000 );
						continue;
					}
					++aid;
				}
				消耗收藏夹( parsedCallback );
				if (reachBoundary) {
					reachBoundary = false;
					System.out.println( "真的达到边界了, 休息10秒,再继续" );
					Thread.sleep( 10000 );
				}
			} catch (Exception ex) {
				FileUtils.writeStringToFile( new File( "error.log" ), ex.getMessage() + "\r\n", true );
				simpleBilibiliService.rebuildContext();
				mainBilibiliService.rebuildContext();
				System.out.println( "出问题了, 睡觉20秒" );
				Thread.sleep( 20000 );
				continue;
			}
		}
	}

	private interface ParsedCallback {
		public void onParsed(Video v);
	}

	private int 消耗收藏夹() {
		return 消耗收藏夹( null );
	}

	private int 消耗收藏夹(final ParsedCallback cb) {
		return db.getVideoDao().callBatchTasks( new Callable<Integer>() {
			public Integer call() throws Exception {
				System.out.println( "消耗收藏夹" );
				int batch = 50;
				int total = 0;
				FavGetList favoriteList = simpleBilibiliService.getFavoriteListJSON( batch );
				while (true) {
					total += favoriteList.vlist.size();
					for (Video v : favoriteList.vlist) {
						if (cb != null)
							cb.onParsed( v );
						db.createOrUpdate( v );
					}
					simpleBilibiliService.deleteFavoriteJSON( favoriteList );
					if (favoriteList.count > favoriteList.vlist.size())
						favoriteList = simpleBilibiliService.getFavoriteListJSON( batch );
					else
						break;
				}
				return total;
			}
		} );
	}

	@Test
	public void 更新所有状态1() {
		List<Video> list = db.getVideoByStateAndTypeID( 1, -1, 50 );
		while (list.size() > 0) {
			for (Video v : list) {
				simpleBilibiliService.addFavotite( v.aid );
			}
			消耗收藏夹();
			list = db.getVideoByStateAndTypeID( 1, -1, 50 );
		}
	}

	@Test
	public void 更新状态() {
		List<Video> list = db.getVideoByState( 1 );
		System.out.println( "totalsize=" + list.size() );
		int count = 0;
		for (Video v : list) {
			Video v2 = simpleBilibiliService.getVideo1( v.aid );
			if (v.state != v2.state) {
				System.out.println( "更新 " + v2 );
				db.update( v2 );
				if (v2.isMQX()) {
					simpleBilibiliService.addFavotite( v2.aid );
					++count;
					if (count > 50) {
						消耗收藏夹();
						count = 0;
					}
				}
			}
		}
		消耗收藏夹();
	}

	@Test
	public void 更新所有还没有获取标题的视频() {
		int batch = 50;
		List<Video> list = db.getMQXList( batch );
		while (list.size() > 0) {
			//加入收藏夹
			for (Video v : list) {
				simpleBilibiliService.addFavotite( v.aid );
			}
			消耗收藏夹();
			list = db.getMQXList( batch );
		}
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

	@Test
	public void 测试阻塞时间() {
		阻塞直到( makeDate( 12, 9, 23, 18 ) );
		System.out.println( "ok" );
	}

	@Test
	public void 抢评论() throws Exception {
		ExecutorService es = Executors.newFixedThreadPool( 1 );//4个线程
		List<Future<?>> list = new ArrayList<Future<?>>();
		List<CommentTask> taskList = new ArrayList<CommentTask>();
		List<Date> taskBlockTimeList = new ArrayList<Date>();
		taskList.add( new CommentTask( 3358188, "整个人都ですわ了" ) );
		taskBlockTimeList.add( makeDate( 12, 9, 23, 23 ) );
		//		taskList.add( new CommentTask( 3347427, "喝大力, 网球打得好." ) );
		//		taskList.add( new CommentTask( 3347425, "喝大力, 不吃JK做的饭." ) );
		//		taskList.add( new CommentTask( 3347415, "喝大力, 我也要做偶像." ) );
		for (int i = 0; i < taskList.size(); ++i) {
			final CommentTask ct = taskList.get( i );
			final Date blockTime=taskBlockTimeList.get( i );
			Future<?> submit = es.submit( new Runnable() {
				public void run() {
					阻塞直到( blockTime );
					while (true) {
						long beg = System.currentTimeMillis();
						String result = mainBilibiliService.comment( ct.aid, ct.msg );
						long end = System.currentTimeMillis();
						System.out.println(
								"对 " + ct.aid + " 进行评论 " + ct.msg + " , 结果是 " + result + " 时间=" + ( end - beg ) );
						if ("OK".equals( result )) {
							break;
						}
					}
				}
			} );
			list.add( submit );
		}
		for (Future<?> f : list) {
			f.get();
		}
		es.shutdown();
	}

	@Test
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

	@Test
	public void 获取番剧每集的aid() {
		String bid = "2744";
		Bangumi b = simpleBilibiliService.getBangumi( bid );
		System.out.println( b.getName() );
		for (int i = 0; i < b.getAids().size(); ++i) {
			System.out.println( b.getAids().get( i ) );
		}
	}

	@Test
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
