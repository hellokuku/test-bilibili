package org.xzc.bilibili.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
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
import org.xzc.bilibili.Sign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CommentWokerThread extends Thread {
	private final Config cfg;
	private final HttpUriRequest req;
	private final AtomicBoolean stop;
	private final AtomicLong last;

	public CommentWokerThread(Config cfg, AtomicBoolean stop, AtomicLong last) {
		this.cfg = cfg;
		this.stop = stop;
		//这里为了节省资源 而将它做成一个成员变量
		this.req = makeCommentRequest( cfg );
		this.last = last;
	}

	private static HttpUriRequest makeCommentRequest(Config cfg) {
		Map<String, String> params = new HashMap<String, String>();
		params.put( "access_key", "339a4620ad6791660e8a49af49af3add" );
		params.put( "appkey", "c1b107428d337928" );
		params.put( "aid", Integer.toString( cfg.getAid() ) );
		Sign s = new Sign( params );
		params.put( "sign", s.getSign() );
		UrlEncodedFormEntity entity = null;
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add( new BasicNameValuePair( "msg", cfg.getMsg() ) );
		list.add( new BasicNameValuePair( "mid", "19161363" ) );
		try {
			entity = new UrlEncodedFormEntity( list, "utf-8" );
		} catch (Exception ex) {
		}
		RequestBuilder rb = RequestBuilder.post( "http://api.bilibili.com/feedback/post" ).setEntity( entity );
		for (Entry<String, String> e : params.entrySet()) {
			rb.addParameter( e.getKey(), e.getValue() );
		}
		return rb.build();
	}

	public void run() {
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( cfg.getBatch() );
		p.setDefaultMaxPerRoute( cfg.getBatch() );
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		CloseableHttpClient hc = HttpClients.custom().setDefaultRequestConfig( rc )
				.setProxy( new HttpHost( cfg.getProxyHost(), cfg.getProxyPort() ) )
				.setConnectionManager( p ).build();
		ExecutorService es = Executors.newFixedThreadPool( cfg.getBatch() );
		while (!work( hc, es )) {
			System.out.println( "[" + cfg.getSubTag() + "] 超速, 休息一下" );
			try {
				Thread.sleep( 2000 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean work(final CloseableHttpClient hc, ExecutorService es) {
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		final AtomicInteger tcount = new AtomicInteger( 0 );
		final AtomicBoolean overspeed = new AtomicBoolean( false );
		//final AtomicLong last = new AtomicLong( 0 );
		final long tbeg = System.currentTimeMillis();
		for (int ii = 0; ii < cfg.getBatch(); ++ii) {
			Future<?> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					while (!stop.get() && !overspeed.get()) {
						try {
							CloseableHttpResponse res = hc.execute( req );
							res.getStatusLine().getStatusCode();
							long end = System.currentTimeMillis();
							long llast = last.getAndSet( end );
							String content = EntityUtils.toString( res.getEntity() );
							HttpClientUtils.closeQuietly( res );
							int count = tcount.incrementAndGet();
							if (count % 10 == 0) {
								System.out.println( cfg.getTag() + " " + cfg.getSubTag() + " " + count + " 时间="
										+ ( end - tbeg ) / 1000 + "秒 间隔="
										+ ( end - llast ) );
							}
							JSONObject json = JSON.parseObject( content );
							int code = json.getIntValue( "code" );
							//我们可以利用Feedback duplicate, 它的code也是0 这样可以防止重复
							//然后由可以利用多线程的并发 简直流弊
							//System.out.println( content );
							if (code == 0) {//{"code":0,"msg":"Feedback duplicate"}
								stop.set( true );

							}
							if (code == -503) {
								//{"code":-503,"result":[],"error":"overspeed"}
								//超速
								overspeed.set( true );
								return null;
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					return null;
				}
			} );
			futureList.add( f );
		}
		for (Future<?> f : futureList)
			try {
				f.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return stop.get();
	}

}
