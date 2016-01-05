package org.xzc.bilibili.autosignin;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.api2.BilibiliService2;
import org.xzc.bilibili.api2.BilibiliService3;
import org.xzc.bilibili.config.DBConfig;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.util.HCs;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DBConfig.class, BatchOpeartionRunner.class })
@Configuration
public class BatchOpeartionRunner {
	private static final Random r = new Random();

	private static final String[] types = new String[] { "广告", "色情", "刷屏", "引战", "剧透", "政治", "人身攻击", "视频不相关" };

	private static void schedule(
			final ScheduledExecutorService ses,
			final AtomicInteger count,
			final int maxCount,
			final BilibiliService2 bs,
			final Account a,
			int delaySeconds,
			final int aid,
			final int rpid) {
		if (count.get() >= maxCount)
			return;
		ses.schedule( new Callable<Void>() {
			public Void call() throws Exception {
				try {
					int type = r.nextInt( types.length );
					//String content = bs.report( a, aid, rpid, type + 1, types[type] );
					//String content = bs.report( a, aid, rpid, type + 1, types[type] );
					String content = bs.report( a, aid, rpid, 0, "视频发布前就评论" );
					//String content = bs.report( a, aid, rpid, 2, "色情" );
					JSONObject json = JSON.parseObject( content );
					int code = json.getIntValue( "code" );
					if (code == 0) {
						int c = count.incrementAndGet();
						System.out.println( c );
						if (c < maxCount)
							schedule( ses, count, maxCount, bs, a, 0, aid, rpid );
					} else if (code == 12019) {
						int ttl = json.getJSONObject( "data" ).getIntValue( "ttl" );
						if (count.get() < maxCount)
							schedule( ses, count, maxCount, bs, a, ttl, aid, rpid );
					} else if (code == 12005) {
						//结束
					} else {
						//12006 是什么?
						System.out.println( content );
						if (count.get() < maxCount)
							schedule( ses, count, maxCount, bs, a, 1, aid, rpid );
					}
				} catch (RejectedExecutionException e) {
					//ignore
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				return null;
			}
		}, delaySeconds, TimeUnit.SECONDS );
	}

	@Autowired
	private RuntimeExceptionDao<Account, Integer> dao;

	private HC hc = HCs.makeHC();

	@Test
	public void 批量举报() throws InterruptedException, SQLException {
		//http://api.bilibili.com/x/reply/info?rpid=72867827&oid=3406582&type=1
		int maxCount = 10000;
		int batch = 64;
		
		//final String aids = "av3477573";final String rpids = "l_id_75273595";
		final String aids = "2573526";final String rpids = "35013384";
		
		//l_id_75273595
		//l_id_75268356
		//l_id_75263126
		final int aid = aids.startsWith( "av" ) ? Integer.parseInt( aids.substring( 2 ) ) : Integer.parseInt( aids );
		final int rpid = rpids.startsWith( "l_id_" ) ? Integer.parseInt( rpids.substring( 5 ) )
				: Integer.parseInt( rpids );

		QueryBuilder<Account, Integer> qb = dao.queryBuilder();
		qb.where().like( "userid", "%sina.com%" );
		List<Account> list = qb.query();

		final ScheduledExecutorService ses = Executors.newScheduledThreadPool( batch );

		final AtomicInteger count = new AtomicInteger( 0 );
		final BilibiliService2 bs = new BilibiliService2();
		bs.setBatch( batch );
		bs.setAutoCookie( false );
		bs.setProxy( "202.195.192.197", 3128 );
		bs.postConstruct();

		for (Account a : list) {
			schedule( ses, count, maxCount, bs, a, 0, aid, rpid );
		}
		ses.shutdown();
		ses.awaitTermination( 1, TimeUnit.HOURS );
	}

	private void 维持赞(BilibiliService3 bs, int aid, int rpid, int finalLike) throws SQLException, InterruptedException {
		QueryBuilder<Account, Integer> qb = dao.queryBuilder();
		qb.where().like( "userid", "%sina.com%" );

		LinkedList<Account> unused = new LinkedList<Account>( qb.query() );
		LinkedList<Account> used = new LinkedList<Account>();

		final AtomicInteger count = new AtomicInteger( 0 );

		while (true) {
			int like = bs.getLike( aid, rpid );
			System.out.println( rpid + "现在的赞是" + like );
			if (like < finalLike) {
				Account a = unused.removeFirst();
				int code = bs.replayAction( a, aid, rpid, 1 );
				used.addLast( a );
			} else if (like > finalLike) {
				Account a = used.removeFirst();
				int code = bs.replayAction( a, aid, rpid, 0 );
				unused.addLast( a );
			}
			Thread.sleep( 1000 );
		}
	}

	@Test
	public void 维持赞() throws SQLException, InterruptedException {
		final BilibiliService3 bs = new BilibiliService3();
		bs.setProxy( "202.195.192.197", 3128 );
		bs.init();
		维持赞( bs, 3515557, 75819075, 20 );
		//维持赞( bs, 3487736, 75043175, 520 );
		//维持赞( bs, 3487736, 75042673, 450 );
	}

	@Test
	public void 批量赞() throws Exception {
		//zan( new ZanConfig( "av3487736", "l_id_75043175", 800, 1, 64 ) );
		//zan( new ZanConfig( "av3487736", "l_id_75042178", 9999, 99, 16 ) );
		//zan( "3515557", "75819075", 20);
		//zan( "av3487736", "l_id_75050273", 250 );
		//zan( "av3487736", "l_id_75042178", 99 );
		//zan( "av3487736", "l_id_75043175", 520 );
		//zan( "av3487736", "l_id_75042673", 450 );
	}

	@After
	public void after() {
	}

	@Before
	public void before() {
	}

	private int getLike(int aid, int rpid) {
		JSONObject json = hc
				.asJSON( Req.get( "http://api.bilibili.com/x/reply/info?type=1" ).params( "oid", aid, "rpid",
						rpid ) );
		int like = json.getJSONObject( "data" ).getIntValue( "like" );
		return like;
	}

	private int getLike(ZanConfig zc) {
		return getLike( zc.aid, zc.rpid );
	}

	//使得这个回复的赞维持在finalLike
	private ZanConfig maintain(String aids, String rpids, int finalLike) {
		int aid = ZanConfig.parseAid( aids );
		int rpid = ZanConfig.parseRpid( rpids );
		int like = getLike( aid, rpid );
		int diff = finalLike - like;
		return new ZanConfig( aid, rpid, diff >= 0 ? diff : -diff, diff >= 0 ? 1 : 0, 1 );
	}

	private void zan(String aids, String rpids, int finalLike) throws Exception {
		zan( maintain( aids, rpids, finalLike ) );
	}

	private void zan(String aids, String rpids, int maxCount, int action) throws Exception {
		zan( new ZanConfig( aids, rpids, maxCount, action ) );
	}

	private void zan(final ZanConfig zc) throws Exception {
		if (zc.maxCount == 0)
			return;
		//List<Account> list = dao.queryForAll();
		QueryBuilder<Account, Integer> qb = dao.queryBuilder();
		qb.where().like( "userid", "%sina.com%" );
		List<Account> list = qb.query();

		ExecutorService es = Executors.newFixedThreadPool( Math.min( list.size(), zc.batch ) );
		final AtomicInteger count = new AtomicInteger( 0 );
		final BilibiliService3 bs = new BilibiliService3();
		bs.setBatch( zc.batch );
		bs.setProxy( "202.195.192.197", 3128 );
		bs.init();
		for (Account aa : list) {
			final Account a = aa;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					if (count.get() >= zc.maxCount)
						return null;
					int code = bs.replayAction( a, zc.aid, zc.rpid, zc.action );
					if (code == 0) {
						count.incrementAndGet();
					}
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
		System.out.println( "成功个数=" + count.get() );
		System.out.println( zc + "现在的赞是" + getLike( zc ) );
	}
}
