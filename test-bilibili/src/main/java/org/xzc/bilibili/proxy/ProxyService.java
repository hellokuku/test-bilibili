package org.xzc.bilibili.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

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
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.xzc.bilibili.util.Sign;

import com.alibaba.fastjson.JSON;

@Component
public class ProxyService {
	private static final String url = "http://61.164.47.167/view?abc";
	private static final int timeout = 2000;

	private static RequestBuilder addUserAgent(RequestBuilder rb) {
		rb.addHeader( "User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" );
		return rb;
	}

	private static RequestConfig makeRC(Proxy p) {
		RequestConfig rc = RequestConfig.custom()
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout )
				.setProxy( new HttpHost( p.getIp(), p.getPort() ) )
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.build();
		return rc;
	}

	private static HttpUriRequest makeRequest(RequestConfig rc) {
		Map<String, String> params = new HashMap<String, String>();
		params.put( "access_key", "abcd"/*cfg.getAccessKey()*/ );
		params.put( "appkey", Sign.appkey );
		params.put( "platform", "android" );
		params.put( "_device", "android" );
		params.put( "type", "json" );
		//params.put( "aid", Integer.toString( cfg.getAid() ) );
		Sign s = new Sign( params );
		params.put( "sign", s.getSign() );
		UrlEncodedFormEntity entity = null;
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		//list.add( new BasicNameValuePair( "msg", cfg.getMsg() ) );
		//list.add( new BasicNameValuePair( "mid", cfg.getMid() ) );
		try {
			entity = new UrlEncodedFormEntity( list, "utf-8" );
		} catch (Exception ex) {
		}
		RequestBuilder rb = RequestBuilder.post( "http://61.164.47.167/feedback/post" ).setEntity( entity );
		rb.setConfig( rc );
		rb.addHeader( "Host", "api.bilibili.com" );
		rb.addHeader( "User-Agent", "Mozilla/5.0 BiliDroid/3.3.0 (bbcallen@gmail.com)" );
		for (Entry<String, String> e : params.entrySet()) {
			rb.addParameter( e.getKey(), e.getValue() );
		}
		return rb.build();
	}

	private CloseableHttpClient hc;

	private int batch = 256;

	public void directlyConnect() {
		RequestConfig rc = makeRC( null );
		long beg = System.currentTimeMillis();
		String content = asString( makeRequest( rc ) );
		System.out.println( content );
		System.out.println( "耗时=" + ( System.currentTimeMillis() - beg ) );
	}

	public int getBatch() {
		return batch;
	}

	public List<Proxy> getProxyList() {
		//目前主要是从http://www.xicidaili.com这个网站拉取数据
		List<Proxy> list = new ArrayList<Proxy>();
		//xici( "http://www.xicidaili.com/nn/", list );
		//xici( "http://www.xicidaili.com/nt/", list );
		//xici( "http://www.xicidaili.com/wn/", list );
		//xici( "http://www.xicidaili.com/wt/", list );
		//kuaidili( "http://www.kuaidaili.com/free/inha/", list );
		//kuaidili( "http://www.kuaidaili.com/free/intr/", list );
		kuaidili( "http://www.kuaidaili.com/free/outha/", list );
		kuaidili( "http://www.kuaidaili.com/free/outtr/", list );
		return list;
	}

	@PostConstruct
	public void postConstruct() {
		PoolingHttpClientConnectionManager p = new PoolingHttpClientConnectionManager();
		p.setMaxTotal( batch * 4 );
		p.setDefaultMaxPerRoute( batch );
		hc = HttpClients.custom()
				.setRetryHandler( new StandardHttpRequestRetryHandler( 10, false ) )
				.setConnectionManager( p ).build();
	}

	public void tryProxy(Proxy p) {
		RequestConfig rc = makeRC( p );
		for (int i = 0; i < 4; ++i) {
			try {
				long beg = System.currentTimeMillis();
				String content = asString( makeRequest( rc ) );
				p.setDuration( System.currentTimeMillis() - beg );
				p.setSuccess( JSON.parseObject( content ).getIntValue( "code" ) == -2 );
			} catch (RuntimeException e) {
				p.setSuccess( false );
			}
			if (p.isSuccess())
				return;
		}
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

	private String asString(String url) {
		return asString( addUserAgent( RequestBuilder.get( url ) )
				.build() );
	}

	private void kuaidili(String baseUrl, List<Proxy> list) {
		for (int page = 1; page <= 10; ++page) {
			String url = baseUrl + page;
			String content = asString( url );
			Document d = Jsoup.parse( content );
			Elements trs = d.select( "#list  tr" );
			for (int i = 1; i < trs.size(); ++i) {
				Element tr = trs.get( i );
				Elements tds = tr.select( "td" );
				String ip = tds.get( 0 ).text().trim();
				int port = Integer.parseInt( tds.get( 1 ).text().trim() );
				Proxy p = new Proxy();
				p.setIp( ip );
				p.setPort( port );
				p.setDescription( tds.get( 4 ).text().trim() );
				list.add( p );
			}
		}
	}

	private void xici(String baseUrl, List<Proxy> list) {
		for (int page = 1; page <= 4; ++page) {
			String url = baseUrl + page;
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
	}
}
