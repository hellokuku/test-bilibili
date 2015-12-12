package org.xzc.bilibili.comment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.alibaba.fastjson.JSON;

public class CommentRunner {
	private static Trigger addJob(Scheduler s, JobDetail job, DateTime startAt, Config cfg)
			throws SchedulerException {
		Trigger t = TriggerBuilder.newTrigger()
				.startAt( startAt.toDate() )
				.forJob( job )
				.usingJobData( CommentJob.ARG_CONFIG, JSON.toJSONString( cfg ) )
				.build();
		s.scheduleJob( t );
		return t;
	}

	public static void main(String[] args) throws Exception {
		StdSchedulerFactory f = new StdSchedulerFactory( "quartz2.properties" );
		Scheduler s = f.getScheduler();
		s.start();

		JobDetail commentJob = JobBuilder.newJob( CommentJob.class )
				.withIdentity( "comment" )
				.storeDurably()
				.build();

		s.addJob( commentJob, false );

		List<Sender> senderList = new ArrayList<Sender>();
		senderList.add( new Sender( "cache.sjtu.edu.cn", 8080, 8, "sjtu" ) );
		senderList.add( new Sender( "202.120.17.158", 2076, 8, "158" ) );
		senderList.add( new Sender( "222.35.17.177", 2076, 8, "177" ) );
		Config cfg = new Config( "一拳超人", 3367236, "怎么会有这个?" ).setSenderList( senderList );
		Trigger t1 = addJob( s, commentJob, DateTime.now().plusSeconds( 3 ), cfg );
		SchedulerMetaData md = s.getMetaData();
		System.out.println( md.getThreadPoolSize() );
	}

	public void 强大的抢评论策略(long delay, int aid, String msg) throws Exception {
		// 几个代理
		//HttpHost proxy1 = new HttpHost( "cache.sjtu.edu.cn", 8080 );
		//HttpHost proxy2 = new HttpHost( "202.120.17.158", 2076 );
		//HttpHost proxy3 = new HttpHost( "222.35.17.177", 2076 );
		//proxy1 = null;
		//proxy2 = null;
		// 每个工作者线程的配置
		/*		Config c0 = new Config( null, 8, aid, msg, null, 0, null, null );
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
					t3.join();*/
	}

	private byte toByte(long a) {
		if (a < 128)
			return (byte) a;
		return (byte) ( a - 256 );
	}

	private static InetAddress toInetAddress(long address) throws UnknownHostException {
		byte[] addr = new byte[4];
		addr[3] = (byte) ( address % 256 );
		address /= 256;
		addr[2] = (byte) ( address % 256 );
		address /= 256;
		addr[1] = (byte) ( address % 256 );
		address /= 256;
		addr[0] = (byte) ( address );
		System.out.println( addr[0] );
		System.out.println( addr[1] );
		System.out.println( addr[2] );
		System.out.println( addr[3] );
		return InetAddress.getByAddress( addr );
	}

	private static long ubyte(byte b) {
		return b >= 0 ? b : ( 256L + b );
	}

	private static long toInt(String address) throws UnknownHostException {
		return toInt( InetAddress.getByName( address ) );
	}

	private static long toInt(InetAddress address) {
		byte[] addr = address.getAddress();
		long ret = ubyte( addr[0] );
		ret = ret * 256 + ubyte( addr[1] );
		ret = ret * 256 + ubyte( addr[2] );
		ret = ret * 256 + ubyte( addr[3] );
		return ret;
	}

	private boolean canConnect(String address, int port) throws UnknownHostException {
		return canConnect( InetAddress.getByName( address ), port );
	}

	private boolean canConnect(InetAddress address, int port) {
		InetSocketAddress isa = new InetSocketAddress( address, port );
		Socket socket = null;
		try {
			socket = new Socket();
			socket.connect( isa, 2000 );
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (socket != null && !socket.isClosed())
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private CloseableHttpClient hc;

	@Before
	public void before() {
		hc = HttpClients.custom().build();
	}

	public void after() {
		HttpClientUtils.closeQuietly( hc );
	}

	public boolean canHttpProxy(String address, int port) throws ClientProtocolException, IOException {
		HttpHost proxy = new HttpHost( address, port );
		CloseableHttpResponse res = null;
		try {
			res = hc.execute(
					RequestBuilder.get( "http://api.bilibili.com/view" )
							.setConfig( RequestConfig.custom().setSocketTimeout( 2000 ).setConnectTimeout( 2000 )
									.setConnectionRequestTimeout( 2000 ).setProxy( proxy ).build() )
							.build() );
			String content = EntityUtils.toString( res.getEntity() );
			res.close();
			return JSON.parseObject( content ).getInteger( "code" ) == -1;
		} catch (Exception e) {
			return false;
		} finally {
			HttpClientUtils.closeQuietly( res );
		}
	}

	@Test
	public void test3() throws ClientProtocolException, IOException {
		System.out.println( canHttpProxy( "218.97.194.198", 80 ) );
	}

	@Test
	public void test2() throws Exception {
		HttpHost proxy1 = new HttpHost( "60.250.79.187", 110 );
		HttpHost proxy2 = new HttpHost( "222.35.17.177", 2076 );
		//HttpHost proxy3 = new HttpHost( "117.136.234.12", 80 );
		HttpHost proxy3 = new HttpHost( "58.251.47.101", 8081 );
		CloseableHttpClient hc = HttpClients.custom().setProxy( proxy3 ).build();
		//CloseableHttpResponse res = hc.execute( RequestBuilder.get( "http://1111.ip138.com/ic.asp" ).build() );
		long beg = System.currentTimeMillis();
		CloseableHttpResponse res = hc.execute( RequestBuilder.get( "http://api.bilibili.com/view" ).build() );
		String content = EntityUtils.toString( res.getEntity(), "gb2312" );
		long end = System.currentTimeMillis();
		boolean ok = JSON.parseObject( content ).getIntValue( "code" ) == -1;
		System.out.println( "耗时" + ( end - beg ) );
		res.close();
		hc.close();
		System.out.println( content );
	}
}