package org.xzc.bilibili.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.xzc.bilibili.Sign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TestHttpClient {

	@Test
	public void test_String_To_JSONArray() {
		JSONArray ja = JSON.parseArray( "[1,\"abc\",'ceshi']" );
		System.out.println( ja );
	}

	@Test
	public void test_String_To_JSONObject() {
		Object obj = JSON.parse( "{name:'xzc',age:20}" );//实际返回是JSONObject
		System.out.println( obj );
	}

	@Test
	public void test_String_To_Object() {
		MyUser1 obj = JSON.parseObject( "{\"Age2\":20,\"id\":13,\"bir\":\"2015-12-06\",\"name\":\"xzc\"}",
				MyUser1.class );
		System.out.println( obj );
		//		obj = JSON.parseObject( "{ID:1,name:'xzc',age:20,password:'bzd',birthday:'2015--1--1'}", MyUser1.class );
		//		System.out.println( obj );
	}

	@Test
	public void test_Object_To_String() {
		MyUser1 u = new MyUser1();
		u.id = 1;
		u.name = "xzc";
		u.age = 20;
		u.birthday = new Date();
		u.password = "bzd";
		System.out.println( JSON.toJSONString( u ) );
	}

	@Test
	public void test_Object_To_JSONObject() {
		MyUser1 u = new MyUser1();
		u.id = 1;
		u.name = "xzc";
		u.age = 20;
		u.birthday = new Date();
		u.password = "bzd";
		System.out.println( JSON.toJSON( u ) );
		//		JSON.toJavaObject( json, clazz )
	}

	//	private String turl = "http://api.bilibili.com/feedback/post";
	private String turl = "http://api.bilibili.com/feedback/post";
	private Random r = new Random();

	private HttpUriRequest makeCommentRequest(int aid, String msg) {
		Map<String, String> params = new HashMap<String, String>();
		params.put( "access_key", "339a4620ad6791660e8a49af49af3add" );
		params.put( "appkey", "c1b107428d337928" );
		params.put( "aid", Integer.toString( aid ) );
		//params.put( "mid", "19161363" );
		//params.put( "msg", msg );
		Sign s = new Sign( params );
		params.put( "sign", s.getSign() );
		UrlEncodedFormEntity entity = null;
		try {
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			//list.add( new BasicNameValuePair( "aid", Integer.toString( aid ) ) );
			list.add( new BasicNameValuePair( "msg", msg ) );
			list.add( new BasicNameValuePair( "mid", "19161363" ) );
			entity = new UrlEncodedFormEntity( list, "utf-8" );
		} catch (Exception ex) {
		}
		RequestBuilder rb = RequestBuilder.post( "http://api.bilibili.com/feedback/post" ).setEntity( entity );
		String ip = "222.35.10." + r.nextInt( 256 );
		rb.addHeader( "X-Client-IP", ip );
		rb.addHeader( "X-Forwarded-For", ip );
		for (Entry<String, String> e : params.entrySet()) {
			rb.addParameter( e.getKey(), e.getValue() );
		}
		return rb.build();
	}

	long last[] = new long[] { 0 };
	int taid[] = new int[] { 3356294 };//3356294 2356223
	private AtomicBoolean ab = new AtomicBoolean();

	private void work(Config cfg) throws Exception {
		ExecutorService es = cfg.es;
		//SocketConfig sc = SocketConfig.custom().setSoKeepAlive( true ).setSoReuseAddress( true ).build();
		final CloseableHttpClient hc = cfg.hc;
		//		final CloseableHttpClient hc = HttpClients.custom().setConnectionManager( p ).build();
		int batch = 4;
		List<Future<?>> futureList = new ArrayList<Future<?>>();

		final boolean ok[] = new boolean[] { false };
		final AtomicInteger ai = new AtomicInteger( 0 );
		final AtomicInteger tcount = new AtomicInteger( 0 );
		final long tbeg = System.currentTimeMillis();
		for (int ii = 0; ii < batch; ++ii) {
			Future<?> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					for (; !ok[0];) {
						long beg = System.currentTimeMillis();
						int aid = taid[0];
						CloseableHttpResponse res = hc.execute( makeCommentRequest( aid, "测试测试6" ) );
						//						System.out.println( "aid=" + aid + ", 结果=" + json.getString( "message" ) + ", 耗时="
						//								+ ( end - beg ) + "间隔=" + ( end - last[0] ) );String content = EntityUtils.toString( res.getEntity(), "utf8" );
						tcount.incrementAndGet();
						long end = System.currentTimeMillis();
						String content = EntityUtils.toString( res.getEntity() );
						HttpClientUtils.closeQuietly( res );
						//System.out.println( tcount.get() + " 时间=" + ( end - tbeg )/1000+"秒 间隔="+(end-last[0]) + content.trim() );
						System.out
								.println( tcount.get() + " 时间=" + ( end - tbeg ) / 1000 + "秒 间隔=" + ( end - last[0] ) );

						last[0] = end;
						JSONObject json = JSON.parseObject( content );
						int code = json.getIntValue( "code" );
						//我们可以利用Feedback duplicate, 它的code也是0 这样可以防止重复
						//然后由可以利用多线程的并发 简直流弊
						//System.out.println( content );
						if (code == 0) {
							ok[0] = true;
							ai.incrementAndGet();
							//{"code":0,"msg":"Feedback duplicate"}
							//{"code":-503,"result":[],"error":"overspeed"}
						}
						if (code == -503) {
							//超速
							System.out.println(
									( System.currentTimeMillis() - tbeg ) / 1000 + "秒 " + tcount.get() + "个" );
							//						System.out.println( bcs );
							//							System.exit( 0 );
							return null;
						}
					}
					return null;
				}
			} );
			futureList.add( f );
		}
		/*for (int i = 0; i < 30; ++i) {
			System.out.println( "睡觉" + i );
			Thread.sleep( 1000 );
		}
		System.out.println( "改变! tid=" + 2356223 );
		taid[0] = 2356223;
		*/
		for (Future<?> f : futureList)
			f.get();
		//System.out.println( "重启" );
		//Thread.sleep( 3000 );
		//work( es );
		if (!ok[0]) {
			Thread.sleep( 2000 );
			work( cfg );
		}
	}

	public static class Config {
		public ExecutorService es;
		public CloseableHttpClient hc;
		public PoolingHttpClientConnectionManager p;
	}
	/*private LinkedBlockingQueue<Config> queue;
	private Thread configProducer=new Thread(){
		@Override
		public void run() {
			super.run();
		}
	};*/

	@Test
	public void test1() throws Exception {
		HttpHost proxy1 = new HttpHost( "cache.sjtu.edu.cn", 8080 );
		HttpHost proxy2 = new HttpHost( "202.120.17.158", 8080 );
		proxy1 = null;
		proxy2 = null;
		ExecutorService es = Executors.newFixedThreadPool( 32 );
		final Config c1 = new Config();
		c1.p = new PoolingHttpClientConnectionManager();
		c1.p.setDefaultMaxPerRoute( 16 );
		c1.p.setMaxTotal( 64 );
		c1.hc = HttpClients.custom().setProxy( proxy1 ).setConnectionManager( c1.p ).build();
		c1.es = es;

		final Config c2 = new Config();
		c2.p = new PoolingHttpClientConnectionManager();
		c2.p.setDefaultMaxPerRoute( 16 );
		c2.p.setMaxTotal( 64 );
		c2.hc = HttpClients.custom().setProxy( proxy2 ).setConnectionManager( c2.p ).build();
		c2.es = es;

		new Thread() {
			public void run() {
				try {
					work( c2 );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		new Thread() {
			public void run() {
				try {
					Thread.sleep( 4000 );
					work( c1 );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		Thread.sleep( 60000 );
		taid[0] = 2356223;
		for (int i = 0; i < 10; ++i) {
			System.out.println( "睡觉" + i );
			Thread.sleep( 1000 );
		}
		//HttpClientUtils.closeQuietly( hc );
		//		System.out.println( "全部完成 ai=" + ai.get() );
	}

	private HttpUriRequest makeCommentRequest2(int aid, String msg) {
		return RequestBuilder.get( "http://interface.bilibili.com/feedback/post" ).addParameter( "callback", "abc" )
				.addParameter( "aid", Integer.toString( aid ) ).addParameter( "msg", msg )
				.addParameter( "action", "send" ).addHeader( "Referer", "http://www.bilibili.com/video/av" + aid )
				.build();
	}

	@Test
	public void testip138() throws Exception {
		CloseableHttpClient hc = HttpClients.custom().build();
		String url = "http://1111.ip138.com/ic.asp";
		String ip = "222.35.11." + r.nextInt( 256 );
		CloseableHttpResponse res = hc.execute(
				RequestBuilder.get( url )
				//.addHeader( "X-Client-IP", ip )
				.addHeader( "X-Forwarded-For", ip )
				.build() );
		String content = EntityUtils.toString( res.getEntity(),"gb2312" );
		System.out.println( content );
		res.close();
		hc.close();
	}

}
