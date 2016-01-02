package org.xzc.bilibili.api2;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.xzc.bilibili.autosignin.ExpState;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.util.Sign;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BilibiliService3 {
	private static final Logger log = Logger.getLogger( BilibiliService3.class );
	private HC hc;
	private int batch = 1;

	public int getBatch() {
		return batch;
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

	private int timeout = 15000;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@PostConstruct
	public void init() {
		RequestConfig rc = RequestConfig.custom()
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout )
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.build();
		PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
		m.setDefaultMaxPerRoute( batch );
		m.setMaxTotal( batch * 2 );
		CloseableHttpClient chc = HttpClients.custom()
				.setConnectionManager( m )
				.setDefaultRequestConfig( rc )
				.setProxy( proxy )
				.build();
		hc = new HC( chc );
	}

	@PreDestroy
	public void close() {
		hc.close();
	}

	public void earnExps(Account a) {

		Req req = ApiUtils.api().post( "/x/share/first" )
				.account( a )
				.datas( "id", 1, "type", 0, "jsonp", "json" );
		hc.consume( req );

		req = ApiUtils.interface0().get( "/player?id=cid:5514894&aid=3471617" ).account( a );
		hc.consume( req );

		/*hc.asString( Req.get( "http://www.bilibili.com/" ).account( a ) );
		hc.asString( Req.get( "http://www.bilibili.com/video/av3471617/" ).account( a ) );
		hc.asString(
				Req.get( "http://www.bilibili.com/api_proxy?app=tag&action=/tags/archive_list&aid=3471617&nomid=1" ) );
				*/
	}

	public ExpState getExpState(Account a) {
		Req req = ApiUtils.accounts().get( "/site/GetExpLog" ).account( a );
		String content = hc.asString( req );
		JSONObject json = JSON.parseObject( content );
		JSONArray ja = json.getJSONObject( "data" ).getJSONArray( "result" );
		int today = DateTime.now().getDayOfYear();
		ExpState es = new ExpState();
		DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern( "yyyy-MM-dd HH:mm:ss" ).toFormatter();
		for (int i = 0; i < ja.size(); ++i) {
			JSONObject jo = ja.getJSONObject( i );
			String reason = jo.getString( "reason" );
			DateTime time = DateTime.parse( jo.getString( "time" ), dtf );
			int doy = time.getDayOfYear();
			if (today == doy) {
				es.login = es.login || "登录奖励".equals( reason );
				es.share = es.share || "分享视频链接被点击奖励".equals( reason );
				es.video = es.video || "观看视频奖励".equals( reason );
			}
		}
		es.updateCount();
		return es;
	}

	public void getUserInfo(Account a) {
		Req req = ApiUtils.api().get( "/myinfo" ).account( a );
		JSONObject json = hc.asJSON( req );
		JSONObject level_info = json.getJSONObject( "level_info" );
		a.coins = json.getIntValue( "coins" );
		a.currentLevel = level_info.getIntValue( "current_level" );
		a.currentMin = level_info.getIntValue( "current_min" );
		a.currentExp = level_info.getIntValue( "current_exp" );
		a.nextExp = level_info.getIntValue( "next_exp" );
	}

	private HttpHost proxy;

	public void setProxy(String ip, int port) {
		proxy = ip == null ? null : new HttpHost( ip, port );
	}

	public int replayAction(Account a, int aid, int rpid, int action) {
		Req req = ApiUtils.api().post( "/x/reply/action" )
				.datas( "jsonp", "jsonp", "oid", aid, "type", 1, "rpid", rpid, "action", action )
				.account( a );
		return hc.asJSON( req ).getIntValue( "code" );
	}

	public int getLike(int aid, int rpid) {
		Req req = ApiUtils.api().get( "/x/reply/info" )
				.datas( "oid", aid, "type", 1, "rpid", rpid );
		return hc.asJSON( req ).getJSONObject( "data" ).getIntValue( "like" );
	}

	public void login2(Account a, ExpState exp) {
		if (a.access_key == null) {
			Sign sign = new Sign( "appkey", Sign.appkey, "userid", a.userid, "pwd", a.password );
			sign.getResult();
			Req req = ApiUtils.api().get( "/login/v2?" + sign.getResult() );
			JSONObject json = hc.asJSON( req );
			a.access_key = json.getString( "access_key" );
		}
		Req req = ApiUtils.api().get( "/myinfo?access_key=" + a.access_key );
		hc.asString( req );
	}
}
