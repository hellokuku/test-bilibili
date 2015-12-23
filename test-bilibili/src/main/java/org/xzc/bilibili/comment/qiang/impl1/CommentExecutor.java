package org.xzc.bilibili.comment.qiang.impl1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.xzc.bilibili.comment.qiang.Proxy;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.util.Utils;

public abstract class CommentExecutor extends Thread {
	private static final Logger log = Logger.getLogger( CommentExecutor.class );

	public enum WorkResult {
		NORMAL, STOP, OVERSPEED, DIU
	}

	protected final CommentConfig cfg;
	protected final AtomicBoolean stop;
	protected final AtomicLong last;
	protected final AtomicInteger count = new AtomicInteger( 0 );
	protected final AtomicInteger diu = new AtomicInteger( 0 );
	protected final AtomicBoolean overspeed = new AtomicBoolean( false );

	public CommentConfig getCommentConfig() {
		return cfg;
	}

	public int getCount() {
		return count.get();
	}

	public int getDiu() {
		return diu.get();
	}

	public CommentExecutor(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		this.cfg = cfg;
		this.stop = stop;
		this.last = last;
	}

	public void run() {
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( cfg.getBatch() * 4 );
		p.setDefaultMaxPerRoute( cfg.getBatch() );
		RequestConfig rc = RequestConfig.custom()
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.setConnectionRequestTimeout( cfg.getTimeout() )
				.setConnectTimeout( cfg.getTimeout() )
				.setSocketTimeout( cfg.getTimeout() )
				.build();
		HttpHost proxy = null;
		Proxy proxyConfig = cfg.getProxy();
		if (proxyConfig != null) {
			if (log.isDebugEnabled())
				log.debug( String.format( "[%s] 设置代理 %s", cfg.getTag(), proxyConfig ) );
			proxy = new HttpHost( proxyConfig.ip, proxyConfig.port );
		}
		CloseableHttpClient chc = HttpClients.custom()
				.setDefaultRequestConfig( rc )
				.setConnectionManager( p )
				.setProxy( proxy )
				.build();

		ExecutorService es = Executors.newFixedThreadPool( cfg.getBatch() );
		if (log.isDebugEnabled()) {
			log.debug( String.format( "[%s] 申请了大小为%d的线程池.", cfg.getTag(), cfg.getBatch() ) );
		}

		try {
			work( chc, es );
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			es.shutdownNow();
			if (log.isDebugEnabled()) {
				log.debug( String.format( "[%s] 关闭了大小为%d的线程池.", cfg.getTag(), cfg.getBatch() ) );
			}
			p.close();
			HttpClientUtils.closeQuietly( chc );
		}
	}

	protected String execute(CloseableHttpClient chc, HttpUriRequest req) throws ClientProtocolException, IOException {
		CloseableHttpResponse res = null;
		try {
			res = chc.execute( req );
			return EntityUtils.toString( res.getEntity() ).trim();
		} finally {
			HttpClientUtils.closeQuietly( res );
		}
	}

	protected abstract HttpUriRequest makeCommentRequest();

	protected void work(final CloseableHttpClient chc, ExecutorService es)
			throws InterruptedException, ExecutionException {

		//生成req
		final HttpUriRequest req = makeCommentRequest();

		//记录future
		List<Future<?>> futureList = new ArrayList<Future<?>>();

		//两个时间点
		final long begAt = System.currentTimeMillis();
		final long endAt = cfg.getEndAt().getTime();
		//batch个线程
		for (int ii = 0; ii < cfg.getBatch(); ++ii) {
			Future<?> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					//没有过期 不要求停止 没有超时
					while (System.currentTimeMillis() < endAt && !stop.get() && !overspeed.get()) {
						try {
							String content = execute( chc, req );
							long now = System.currentTimeMillis();
							long llast = last.getAndSet( now );
							int count1 = count.incrementAndGet();

							if (count1 % cfg.getInterval() == 0) {
								System.out.println( content.length() > 100 ? "过长的文本" : content );
								System.out.println( String.format( "[%s] count=%d diu=%d 已执行%d秒 上一次间隔=%d毫秒",
										cfg.getTag(), count1, diu.get(), ( now - begAt ) / 1000, now - llast ) );
							}
							WorkResult wr = workInternal( content );
							switch (wr) {
							case DIU:
								Utils.sleep( 500 );
								break;
							case NORMAL:
								break;
							case OVERSPEED:
								overspeed.set( true );
								break;
							case STOP:
								stop.set( true );
								return null;
							}
						} catch (IOException ex) {
							//忽略
						} catch (Exception ex) {//其他异常就打印一下
							ex.printStackTrace();
						}
					}
					return null;
				}

			} );
			futureList.add( f );
		}
		Utils.blockUntil( futureList );
	}

	protected abstract WorkResult workInternal(String content);
}
