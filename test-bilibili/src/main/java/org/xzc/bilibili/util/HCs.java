package org.xzc.bilibili.util;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.xzc.bilibili.proxy.Proxy;
import org.xzc.http.HC;

public class HCs {
	public static final int DEFAULT_TIMEOUT = 10000;
	public static final boolean DEFAULT_IGNORE_COOKIE = true;
	private static final int DEFAULT_BATCH = 2;

	public static HC makeHC(String host, int port, boolean ignoreCookie) {
		return makeHC( DEFAULT_TIMEOUT, host, port, ignoreCookie );
	}

	public static HC makeHC(int timeout, int batch, String host, int port, boolean ignoreCookie) {
		Builder b = RequestConfig.custom()
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout );
		if (ignoreCookie) {
			b.setCookieSpec( CookieSpecs.IGNORE_COOKIES );
		}
		RequestConfig rc = b.build();
		HttpHost proxy = null;
		if (host != null)
			proxy = new HttpHost( host, port );
		PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
		m.setMaxTotal( batch * 2 );
		m.setDefaultMaxPerRoute( batch );
		CloseableHttpClient chc = HttpClients.custom()
				.setProxy( proxy )
				.setConnectionManager( m )
				.setDefaultRequestConfig( rc )
				.build();
		return new HC( chc );
	}

	public static HC makeHC(int timeout, String host, int port, boolean ignoreCookie) {
		return makeHC( timeout, DEFAULT_BATCH, host, port, ignoreCookie );
	}

	public static HC makeHC() {
		return makeHC( DEFAULT_IGNORE_COOKIE );
	}

	public static HC makeHC(boolean ignoreCookie) {
		return makeHC( DEFAULT_TIMEOUT, null, 0, ignoreCookie );
	}

	public static HC makeHC(Proxy p) {
		if (p == null)
			return makeHC( null, 0, DEFAULT_IGNORE_COOKIE );
		return makeHC( p.getIp(), p.getPort(), DEFAULT_IGNORE_COOKIE );
	}

	public static HC makeHC(int timeout) {
		return makeHC( timeout, DEFAULT_BATCH, null, 0, DEFAULT_IGNORE_COOKIE );
	}
}
