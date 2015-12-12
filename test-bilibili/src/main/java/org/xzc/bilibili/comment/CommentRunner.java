package org.xzc.bilibili.comment;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class CommentRunner {
	private static long makeDelay(int month, int day, int hour, int minute) {
		Calendar c = Calendar.getInstance();
		c.set( Calendar.MONTH, month - 1 );
		c.set( Calendar.DAY_OF_MONTH, day );
		c.set( Calendar.HOUR_OF_DAY, hour );
		c.set( Calendar.MINUTE, minute );
		return c.getTime().getTime() - new Date().getTime();
	}

	@Test
	public void runnn() throws Exception {
		//强大的抢评论策略( makeDelay( 12, 12, 1, 3 ), 3367052, "快期末考了,求保佑." );
		//强大的抢评论策略( makeDelay( 12, 12, 2, 38 ), 3367059, "好番就是耐看." );
		//强大的抢评论策略( makeDelay( 12, 12, 2, 58 ), 3367069, "上周的9.5吓坏了." );
		强大的抢评论策略( 0, 3367236, "测试测试" );
	}

	public void 强大的抢评论策略(long delay, int aid, String msg) throws Exception {
		// 几个代理
		HttpHost proxy1 = new HttpHost( "cache.sjtu.edu.cn", 8080 );
		HttpHost proxy2 = new HttpHost( "202.120.17.158", 2076 );
		HttpHost proxy3 = new HttpHost( "222.35.17.177", 2076 );
		//proxy1 = null;
		//proxy2 = null;
		// 每个工作者线程的配置
		Config c0 = new Config( null, 8, aid, msg, null, 0, null, null );
		Config c1 = c0.copy().setTag( "AAA" ).setProxy( proxy1 ).setDelay( delay );//.setSip( "61.164.47.167" );
		Config c2 = c0.copy().setTag( "BBB" ).setProxy( proxy2 ).setDelay( delay );//.setSip( "61.164.47.167" );//.setFip( "61.164.47.167" );
		Config c3 = c0.copy().setTag( "CCC" ).setProxy( proxy3 ).setDelay( delay );//.setSip( "112.25.85.6" );//.setDelay( 0 );
		AtomicBoolean stop = new AtomicBoolean( false );
		CommentWokerThread t1 = new CommentWokerThread( c1, stop );
		CommentWokerThread t2 = new CommentWokerThread( c2, stop );
		CommentWokerThread t3 = new CommentWokerThread( c3, stop );
		//t1.start();
		//t2.start();
		t3.start();
		if (t1.isAlive())
			t1.join();
		if (t2.isAlive())
			t2.join();
		if (t3.isAlive())
			t3.join();
	}

	public void testEs() throws InterruptedException, ExecutionException {
		ExecutorService es = Executors.newFixedThreadPool( 2 );
		Future<Void> f1 = es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				for (int i = 0; i < 2; ++i) {
					System.out.println( "1睡觉" + i );
					Thread.sleep( 1000 );
				}
				return null;
			}
		} );
		Future<Void> f2 = es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				for (int i = 0; i < 2; ++i) {
					System.out.println( "2睡觉" + i );
					Thread.sleep( 1000 );
				}
				return null;
			}
		} );
		f1.get();
		f2.get();
		System.out.println( "OK" );
	}

	public void test2() throws Exception {
		CloseableHttpClient hc = HttpClients.custom().build();
		HttpUriRequest req = RequestBuilder.get( "http://112.25.85.6/view" )
				.addHeader( "Host", "api.bilibili.com" )
				.build();
		CloseableHttpResponse res = hc.execute( req );
		String content = EntityUtils.toString( res.getEntity() );
		System.out.println( content );
		res.close();
		hc.close();
	}
}