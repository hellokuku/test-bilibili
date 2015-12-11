package org.xzc.bilibili.model;

import org.apache.http.HttpHost;
import org.junit.Test;
import org.xzc.bilibili.comment.CommentWakerThread;

public class TestHttpClient {
	@Test
	public void test1() throws Exception {
		HttpHost proxy1 = new HttpHost( "cache.sjtu.edu.cn", 8080 );
		HttpHost proxy2 = new HttpHost( "202.120.17.158", 8080 );
		Config c1 = new Config( "AAA", 8, 3356294, "测试测试7", proxy1, 0 );
		Config c2 = new Config( "BBB", 8, 3356294, "测试测试7", proxy2, 4000 );
		Config c3 = new Config( "CCC", 8, 3356294, "测试测试7", null, 0 );
		CommentWakerThread t1 = new CommentWakerThread( c1 );
		CommentWakerThread t2 = new CommentWakerThread( c2 );
		CommentWakerThread t3 = new CommentWakerThread( c3 );
		t1.start();
		t2.start();
		t3.start();
		Thread.sleep( 10000 );
		c1.aid = c2.aid = c3.aid = 2356223;
		t1.join();
		t2.join();
		t3.join();
	}
}