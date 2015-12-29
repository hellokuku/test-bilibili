package org.xzc.bilibili.util;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.xzc.bilibili.proxy.Proxy;
import org.xzc.http.HC;

public class HCs {
	public static HC makeHC(String host, int port, boolean ignoreCookie) {
		return makeHC( 10000, host, port, ignoreCookie );
	}

	public static HC makeHC(int timeout, String host, int port, boolean ignoreCookie) {
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
		CloseableHttpClient chc = HttpClients.custom()
				.setProxy( proxy )
				.setDefaultRequestConfig( rc )
				.build();
		return new HC( chc );

	}

	public static HC makeHC() {
		return makeHC( true );
	}

	public static HC makeHC(boolean ignoreCookie) {
		return makeHC( 10000, null, 0, ignoreCookie );
	}

	public static HC makeHC(Proxy p) {
		if (p == null)
			return makeHC( null, 0, true );
		return makeHC( p.getIp(), p.getPort(), true );
	}

	public static HC makeHC(int timeout) {
		return makeHC( timeout, null, 0, true );
	}
}
