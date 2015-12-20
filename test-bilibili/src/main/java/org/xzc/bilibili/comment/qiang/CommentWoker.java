package org.xzc.bilibili.comment.qiang;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.xzc.bilibili.util.Utils;

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
		CloseableHttpClient hc = HttpClients.custom()
				.setDefaultRequestConfig( rc )
				.setConnectionManager( p )
				.build();
		ExecutorService es = Executors.newFixedThreadPool( cfg.getBatch() );
		int count = 0;
		try {
			System.out.println( "开始执行 " + cfg );
			count = work( hc, es );
		} finally {
			System.out.println( cfg.getTag() + " 执行完毕! count=" + count );
			es.shutdown();
			p.close();
			HttpClientUtils.closeQuietly( hc );
		}
	}

	private static Pattern RESULT_PATTERN = Pattern.compile( "abc\\(\"(.+)\"\\)" );

	private int work(final CloseableHttpClient hc, ExecutorService es) {
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		final AtomicInteger tcount = new AtomicInteger( 0 );
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
								//丢包了
								res.close();
								continue;
							}
							content = Utils.decodeUnicode( content );
							res.close();
							int count = tcount.incrementAndGet();
							//System.out.println( content );
							if (count % interval == 0) {
								System.out.println( content );
								System.out.println( "[" + cfg.getTag() + "] " + count + " 时间="
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
		return tcount.get();
	}

}
