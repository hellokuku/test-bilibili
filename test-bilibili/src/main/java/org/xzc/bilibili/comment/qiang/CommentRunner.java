package org.xzc.bilibili.comment.qiang;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.alibaba.fastjson.JSON;

public class CommentRunner {
	private static final SimpleDateFormat SDF = new SimpleDateFormat( "MM月dd日HH时mm分ss秒" );

	private static Trigger addJob(Scheduler s, JobDetail job, Config cfg) throws SchedulerException {
		Trigger t = TriggerBuilder.newTrigger().startAt( cfg.getStartAt() ).forJob( job )
				.usingJobData( CommentJob.ARG_CONFIG, JSON.toJSONString( cfg ) ).build();
		s.scheduleJob( t );
		System.out.println( "[" + cfg.getTag() + "] 将会于" + SDF.format( cfg.getStartAt() ) + "开始, 于"
				+ SDF.format( cfg.getEndAt() ) + "结束." );
		return t;
	}

	public static void main(String[] args) throws Exception {
		StdSchedulerFactory f = new StdSchedulerFactory( "quartz2.properties" );
		Scheduler s = f.getScheduler();
		s.start();

		JobDetail commentJob = JobBuilder.newJob( CommentJob.class ).withIdentity( "comment" ).storeDurably().build();

		s.addJob( commentJob, false );

		List<Sender> senderList = new ArrayList<Sender>();
				senderList.add( new Sender( "cache.sjtu.edu.cn", 8080, 32, "sjtu" ) );
				senderList.add( new Sender( "202.120.17.158", 2076, 32, "158" ) );
		//		senderList.add( new Sender( "222.35.17.177", 2076, 16, "177" ) );

				senderList.add( new Sender( "27.115.75.114", 8080, 16, "代理1" ) );//100
				senderList.add( new Sender( "112.25.41.136", 80, 16, "代理2" ) );//100
		//下面的延迟大概都是200
				senderList.add( new Sender( "120.52.73.11", 8080, 16, "代理3" ) );
		//以下延迟300
				senderList.add( new Sender( "120.52.73.13", 8080, 16, "代理4" ) );
		//senderList.add( new Sender( "120.52.73.20", 8080, 1, "代理5" ) );
		//senderList.add( new Sender( "120.52.73.21", 80, 1, "代理6" ) );
		//senderList.add( new Sender( "120.52.73.24", 80, 1, "代理7" ) );
		//senderList.add( new Sender( "120.52.73.27", 80, 1, "代理8" ) );
		//senderList.add( new Sender( "120.52.73.29", 8080,1, "代理9" ) );
		senderList.add( new Sender( "116.246.6.52", 801, 32, "代理10" ) );
		senderList.add( new Sender( "122.72.33.139", 801, 32, "代理11" ) );
		senderList.add( new Sender( "112.25.41.136", 801, 32, "代理12" ) );
		//senderList.add( new Sender( null, 0, 32, "本机" ) );

		addJob( s, commentJob, new Config( "雨色可可", 3381912, "雨色可可",
				new DateTime( 2015, 12, 12, 23, 13 ).toDate(),
				new DateTime( 2015, 12, 13, 23, 25 ).toDate() ).setSenderList( senderList ) );
		addJob( s, commentJob, new Config( "温泉幼精箱根酱", 3381920, "温泉幼精箱根酱",
				new DateTime( 2015, 12, 14, 0, 3 ).toDate(),
				new DateTime( 2015, 12, 14, 0, 15 ).toDate() ).setSenderList( senderList ) );
		addJob( s, commentJob, new Config( "魔鬼恋人", 3382145, "魔鬼恋人",
				new DateTime( 2015, 12, 14, 0, 28 ).toDate(),
				new DateTime( 2015, 12, 14, 0, 40 ).toDate() ).setSenderList( senderList ) );
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
			res = hc.execute( RequestBuilder
					.get( "http://api.bilibili.com/view" )
					.setConfig(
							RequestConfig.custom().setSocketTimeout( 2000 ).setConnectTimeout( 2000 )
									.setConnectionRequestTimeout( 2000 ).setProxy( proxy ).build() ).build() );
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
		//HttpHost proxy3 = new HttpHost( "58.251.47.101", 8081 );
		CloseableHttpClient hc = HttpClients.custom().setProxy( null ).build();
		//CloseableHttpResponse res = hc.execute( RequestBuilder.get( "http://1111.ip138.com/ic.asp" ).build() );
		long beg = System.currentTimeMillis();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add( new BasicNameValuePair( "access_key", "339a4620ad6791660e8a49af49af3add" ) );
		params.add( new BasicNameValuePair( "aid", "3334538" ) );
		UrlEncodedFormEntity e = new UrlEncodedFormEntity( params );
		CloseableHttpResponse res = hc.execute( RequestBuilder.post( "http://api.bilibili.com/feedback/post" )
				.addParameter( "mid", "19161363" ).addParameter( "type", "json" )
				.addParameter( "access_key", "339a4620ad6791660e8a49af49af3add" ).addParameter( "msg", "网络好卡啊, 怎么回事." )
				.setEntity( e ).build() );
		String content = EntityUtils.toString( res.getEntity() );
		long end = System.currentTimeMillis();
		System.out.println( content );
		System.out.println( "耗时" + ( end - beg ) );
		res.close();
		hc.close();
		System.out.println( content );
	}
}