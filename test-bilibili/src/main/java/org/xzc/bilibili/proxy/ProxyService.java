package org.xzc.bilibili.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

@Component
public class ProxyService {
	private CloseableHttpClient hc;

	@PostConstruct
	public void postConstruct() {
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( 32 );
		p.setDefaultMaxPerRoute( 16 );
		hc = HttpClients.custom().setRetryHandler( new StandardHttpRequestRetryHandler(10,false) ).setConnectionManager( p ).build();
	}

	private String url = "http://api.bilibili.com/view?abc";

	public void tryProxy(Proxy p) {
		try {
			long beg = System.currentTimeMillis();
			String content = asString( RequestBuilder.get( url ).setConfig(
					RequestConfig.custom()
							.setSocketTimeout( 2000 )
							.setConnectTimeout( 2000 )
							.setConnectionRequestTimeout( 2000 )
							.setProxy( new HttpHost( p.getIp(), p.getPort() ) )
							.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
							.build() )
					.build() );
			p.setDuration( System.currentTimeMillis() - beg );
			p.setSuccess( JSON.parseObject( content ).getIntValue( "code" ) == -1 );
		} catch (RuntimeException e) {
			p.setSuccess( false );
		}
	}

	public List<Proxy> getProxyList() {
		List<Proxy> list = new ArrayList<Proxy>();
		for (int page = 1; page <= 10; ++page) {
			String url = "http://www.xicidaili.com/nn/" + page;
			String content = asString( url );
			Document d = Jsoup.parse( content );
			Elements trs = d.select( "#ip_list  tr" );
			for (int i = 1; i < trs.size(); ++i) {
				Element tr = trs.get( i );
				Elements tds = tr.select( "td" );
				String ip = tds.get( 2 ).text().trim();
				int port = Integer.parseInt( tds.get( 3 ).text().trim() );
				Proxy p = new Proxy();
				p.setIp( ip );
				p.setPort( port );
				p.setDescription( tds.get( 4 ).text().trim() );
				list.add( p );
			}
		}
		return list;
	}

	private String asString(String url) {
		return asString( addUserAgent( RequestBuilder.get( url ) )
				.build() );
	}

	private static RequestBuilder addUserAgent(RequestBuilder rb) {
		rb.addHeader( "User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" );
		return rb;
	}

	private String asString(HttpUriRequest req) {
		CloseableHttpResponse res = null;
		try {
			res = hc.execute( req );
			return EntityUtils.toString( res.getEntity() );
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException( e );
		} finally {
			HttpClientUtils.closeQuietly( res );
		}
	}

	private int timeout = 2000;

	public void directlyConnect() {
		long beg = System.currentTimeMillis();
		String content = asString( RequestBuilder.get( url ).setConfig(
				RequestConfig.custom()
						.setSocketTimeout( timeout )
						.setConnectTimeout( timeout )
						.setConnectionRequestTimeout( timeout )
						//.setProxy( new HttpHost( p.getIp(), p.getPort() ) )
						.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
						.build() )
				.build() );
		System.out.println( content );
		System.out.println( "耗时=" + ( System.currentTimeMillis() - beg ) );
	}
}
