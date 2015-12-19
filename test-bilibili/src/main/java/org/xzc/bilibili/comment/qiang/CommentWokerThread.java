package org.xzc.bilibili.comment.qiang;

import java.util.ArrayList;
import java.util.Date;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xzc.bilibili.util.Sign;
import org.xzc.bilibili.util.Utils;

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

	private static HttpUriRequest makeCommentRequest2(Config cfg) {
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

	private static HttpUriRequest makeCommentRequest(Config cfg) {
		//String sip = "60.221.255.15"; 113.105.152.207 61.164.47.167 112.25.85.6
		String sip = "112.25.85.6";
		return RequestBuilder.get( "http://" + sip + "/feedback/post" )
				.addHeader( "Cookie", "DedeUserID=19480366; SESSDATA=f3e878e5,1450537949,61e7c5d1;" )
				.addHeader( "Host", "interface.bilibili.com" )
				//duruofeixh8
				//.addHeader( "Cookie", "DedeUserID=19557513; SESSDATA=315c6283,1451014585,d1ef321d;" )
				.addParameter( "callback", "abc" )
				.addParameter( "aid", Integer.toString( cfg.getAid() ) ).addParameter( "msg", cfg.getMsg() )
				.addParameter( "action", "send" )
				.addHeader( "Referer", "http://www.bilibili.com/video/av" + cfg.getAid() )
				.build();
	}

	public void run() {
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( cfg.getBatch() * 4 );
		p.setDefaultMaxPerRoute( cfg.getBatch() );
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		HttpClientBuilder hcb = HttpClients.custom()
				.setDefaultRequestConfig( rc )
				.setConnectionManager( p );
		if (cfg.getProxyHost() != null) {
			hcb.setProxy( new HttpHost( cfg.getProxyHost(), cfg.getProxyPort() ) );
		}
		CloseableHttpClient hc = hcb.build();
		System.out.println( cfg.getBatch() );
		ExecutorService es = Executors.newFixedThreadPool( cfg.getBatch() );
		work( hc, es );
	}

	private static Pattern RESULT_PATTERN = Pattern.compile( "abc\\(\"(.+)\"\\)" );

	private void work(final CloseableHttpClient hc, ExecutorService es) {
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		final AtomicInteger tcount = new AtomicInteger( 0 );
		final long tbeg = System.currentTimeMillis();
		final long endAt = cfg.getEndAt().getTime();
		for (int ii = 0; ii < cfg.getBatch(); ++ii) {
			Future<?> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					while (!stop.get()) {
						try {
							CloseableHttpResponse res = hc.execute( req );
							long end = System.currentTimeMillis();
							if (end >= endAt)
								stop.set( true );
							long llast = last.getAndSet( end );
							String content = EntityUtils.toString( res.getEntity() ).trim();
							content = Utils.decodeUnicode( content );
							res.close();
							int count = tcount.incrementAndGet();
							//System.out.println( content );
							if (count % 1000 == 0) {
								System.out.println( content );
								System.out.println( "[" + cfg.getTag() + "," + cfg.getSubTag() + "] " + count + " 时间="
										+ ( end - tbeg ) / 1000 + "秒 间隔="
										+ ( end - llast ) );
							}
							Matcher m = RESULT_PATTERN.matcher( content );
							if (m.find()) {
								String code = m.group( 1 );
								if ("OK".equals( code ) || code.contains( "验证码" )) {
									stop.set( true );
								}
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
	}

}
