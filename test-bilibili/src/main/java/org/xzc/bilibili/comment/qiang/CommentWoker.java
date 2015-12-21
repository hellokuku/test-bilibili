package org.xzc.bilibili.comment.qiang;

import java.net.SocketTimeoutException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xzc.bilibili.util.Sign;
import org.xzc.bilibili.util.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CommentWoker {
	private final Config cfg;
	private final HttpUriRequest req;
	private final AtomicBoolean stop;
	private final AtomicLong last;

	public CommentWoker(Config cfg) {
		this.cfg = cfg;
		this.stop = new AtomicBoolean( false );
		//这里为了节省资源 而将它做成一个成员变量
		this.req = makeCommentRequest( cfg );
		this.last = new AtomicLong( 0 );
	}

	private static HttpUriRequest makeCommentRequest(Config cfg) {
		switch (cfg.getMode()) {
		case 0:
			return makeCommentRequest0( cfg );
		case 1:
			return makeCommentRequest1( cfg );
		}
		throw new IllegalArgumentException( "不合法的mode" );
	}

	private static HttpUriRequest makeCommentRequest1(Config cfg) {
		Map<String, String> params = new HashMap<String, String>();
		params.put( "access_key", cfg.getAccessKey() );
		params.put( "appkey", Sign.appkey );
		params.put( "platform", "android" );
		params.put( "_device", "android" );
		params.put( "type", "json" );
		params.put( "aid", Integer.toString( cfg.getAid() ) );
		Sign s = new Sign( params );
		params.put( "sign", s.getSign() );
		UrlEncodedFormEntity entity = null;
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add( new BasicNameValuePair( "msg", cfg.getMsg() ) );
		list.add( new BasicNameValuePair( "mid", cfg.getMid() ) );
		try {
			entity = new UrlEncodedFormEntity( list, "utf-8" );
		} catch (Exception ex) {
		}
		RequestBuilder rb = RequestBuilder.post( "http://" + cfg.getSip() + "/feedback/post" ).setEntity( entity );
		rb.addHeader( "Host", "api.bilibili.com" );
		rb.addHeader( "User-Agent", "Mozilla/5.0 BiliDroid/3.3.0 (bbcallen@gmail.com)" );
		for (Entry<String, String> e : params.entrySet()) {
			rb.addParameter( e.getKey(), e.getValue() );
		}
		return rb.build();
	}

	private static HttpUriRequest makeCommentRequest0(Config cfg) {
		return RequestBuilder.get( "http://" + cfg.getSip() + "/feedback/post" )
				.addHeader( "Cookie", "DedeUserID=" + cfg.getDedeUserID() + "; SESSDATA=" + cfg.getSESSDATA() + ";" )
				.addHeader( "Host", "interface.bilibili.com" )
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
		int timeout = 10000;
		RequestConfig rc = RequestConfig.custom()
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout )
				.build();
		CloseableHttpClient chc = HttpClients.custom()
				.setDefaultRequestConfig( rc )
				.setConnectionManager( p )
				.build();
		ExecutorService es = Executors.newFixedThreadPool( cfg.getBatch() );
		AtomicInteger tcount = new AtomicInteger( 0 );
		AtomicInteger tdiu = new AtomicInteger( 0 );
		try {
			System.out.println( "开始执行 " + cfg );
			while (!stop.get()) {
				switch (cfg.getMode()) {
				case 0:
					work0( chc, es, tcount, tdiu );
					break;
				case 1:
					work1( chc, es, tcount, tdiu );
					break;
				default:
					throw new IllegalArgumentException( "不合法的mode" );
				}
				System.out.println( cfg.getTag() + " 超速, 休息2秒 count=" + tcount.get() + " diu=" + tdiu.get() );
				Thread.sleep( 2000 );
				tcount.set( 0 );
				tdiu.set( 0 );
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println( cfg.getTag() + " 执行完毕! count=" + tcount.get() + " diu=" + tdiu.get() );
			es.shutdown();
			p.close();
			HttpClientUtils.closeQuietly( chc );
		}
	}

	private static Pattern RESULT_PATTERN = Pattern.compile( "abc\\(\"(.+)\"\\)" );

	private void work0(final CloseableHttpClient hc, ExecutorService es, final AtomicInteger tcount,
			final AtomicInteger tdiu) {
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		final long tbeg = System.currentTimeMillis();
		final long endAt = cfg.getEndAt().getTime();
		final int interval = cfg.getInterval();
		final boolean stopWhenForbidden = cfg.isStopWhenForbidden();
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
							if (content.length() > 100) {
								tdiu.incrementAndGet();
								//丢包了
								res.close();
								continue;
							}
							content = Utils.decodeUnicode( content );
							res.close();
							int count = tcount.incrementAndGet();
							if (count % interval == 0) {
								System.out.println( content );
								System.out
										.println(
												"[" + cfg.getTag() + "] count=" + count + " diu=" + tdiu.get() + " 时间="
														+ ( end - tbeg ) / 1000 + "秒 间隔="
														+ ( end - llast ) );
							}
							Matcher m = RESULT_PATTERN.matcher( content );
							if (m.find()) {
								String code = m.group( 1 );
								if ("OK".equals( code ) || code.contains( "验证码" )
										|| ( stopWhenForbidden && code.contains( "禁言" ) )) {
									stop.set( true );
								}
							}
						} catch (ConnectTimeoutException ex) {
							//忽略
						} catch (SocketTimeoutException ex) {
							//忽略
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
		futureList.clear();
	}

	private void work1(final CloseableHttpClient hc, ExecutorService es, final AtomicInteger tcount,
			final AtomicInteger tdiu) {
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		final long tbeg = System.currentTimeMillis();
		final long endAt = cfg.getEndAt().getTime();
		final int interval = cfg.getInterval();
		final AtomicBoolean overspeed = new AtomicBoolean( false );
		for (int ii = 0; ii < cfg.getBatch(); ++ii) {
			Future<?> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					while (!stop.get() && !overspeed.get()) {
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
							JSONObject json = JSON.parseObject( content );
							int code = json.getIntValue( "code" );
							if (count % interval == 0 || ( code != 0 && code != -404 && code != -503 )) {
								System.out.println( content );
								System.out
										.println(
												"[" + cfg.getTag() + "] count=" + count + " diu=" + tdiu.get() + " 时间="
														+ ( end - tbeg ) / 1000 + "秒 间隔="
														+ ( end - llast ) );
							}
							if (code == 0) {//成功
								stop.set( true );
							} else if (code == -404) {//还不可评论
							} else if (code == -503) {//超速
								overspeed.set( true );
							} else if (code == -103) {
								//{"code":-103,"message":"Credits is not enought.","ts":1450673739}
								//该怎么解决?
								overspeed.set( true );
							} else {
								System.out.println( "遇到其他情况" );
								System.out.println( content );
								System.exit( 0 );
								//其他情况
							}
						} catch (ConnectTimeoutException ex) {
							//忽略
						} catch (SocketTimeoutException ex) {
							//忽略
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
		futureList.clear();
	}

}
