package org.xzc.bilibili.api2;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.util.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestBilibiliService2.class })
@Configuration
public class TestBilibiliService2 {

	@Test
	public void testBS3() throws InterruptedException {
		BilibiliService3 bs = new BilibiliService3();
		bs.setBatch( 5 );
		bs.init();
		
		Account a2 = new Account( "duruofeixh8@163.com", Utils.PASSWORD );
		Account a3 = new Account( "duruofeixh9@163.com", Utils.PASSWORD );
		Account a4 = new Account( "duruofeixh10@163.com", Utils.PASSWORD );
		Account a5 = new Account( "duruofeixh11@163.com", Utils.PASSWORD );
		bs.initAccount( a2 );
		bs.initAccount( a3 );
		bs.initAccount( a4 );
		bs.initAccount( a5 );
		System.out.println( a2 );
		System.out.println( a3 );
		System.out.println( a4 );
		System.out.println( a5 );

		int aid = 3526096;//3526157
		//3526096
		Thread t1 = new Thread( new VideoStatusScanner1( "方法1", bs, aid ) );
		Thread t2 = new Thread( new VideoStatusScanner2( "方法2", bs, aid, a2 ) );
		Thread t3 = new Thread( new VideoStatusScanner3( "方法3", bs, aid, a3 ) );
		Thread t4 = new Thread( new VideoStatusScanner4( "方法4", bs, aid, a4 ) );
		Thread t5 = new Thread( new VideoStatusScanner5( "方法5", bs, aid, a5 ) );
		List<Thread> ts = Arrays.asList( t1, t2, t3, t4, t5 );
		for (Thread t : ts)
			t.start();
		for (Thread t : ts)
			t.join();
	}
}
