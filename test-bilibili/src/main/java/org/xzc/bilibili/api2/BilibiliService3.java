package org.xzc.bilibili.api2;

import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.xzc.bilibili.autosignin.ExpState;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.util.Sign;
import org.xzc.bilibili.util.Utils;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BilibiliService3 {
	private static final Logger log = Logger.getLogger( BilibiliService3.class );
	private HC hc;
	private int batch = 2;

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

	public void setHC(HC hc) {
		this.hc = hc;
	}

	@PostConstruct
	public void init() {
		if (hc != null)
			return;
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
		Req req = ApiUtils.account().get( "/site/GetExpLog" ).account( a );
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
		a.mid = json.getIntValue( "mid" );
		a.name = json.getString( "uname" );
		a.coins = json.getIntValue( "coins" );
		a.currentLevel = level_info.getIntValue( "current_level" );
		a.currentMin = level_info.getIntValue( "current_min" );
		a.currentExp = level_info.getIntValue( "current_exp" );
		a.nextExp = level_info.getIntValue( "next_exp" );
	}

	public void initFavBox(Account a) {
		Req req = ApiUtils.space().get( "/ajax/fav/getBoxList?mid=" + a.mid );
		a.fid = hc.asJSON( req ).getJSONObject( "data" ).getJSONArray( "list" ).getJSONObject( 0 )
				.getIntValue( "fav_box" );
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
				.params( "oid", aid, "type", 1, "rpid", rpid );
		String content = hc.asString( req );
		return JSON.parseObject( content ).getJSONObject( "data" ).getIntValue( "like" );
	}

	public void login2(Account a, ExpState exp) {
		Req req;
		if (a.access_key == null) {
			Sign sign = new Sign( "appkey", Sign.appkey, "userid", a.userid, "pwd", a.password );
			sign.getResult();
			req = ApiUtils.api().get( "/login/v2?" + sign.getResult() );
			JSONObject json = hc.asJSON( req );
			a.access_key = json.getString( "access_key" );
		}
		req = ApiUtils.api().get( "/myinfo?access_key=" + a.access_key );
		hc.asString( req );
	}

	public String updateSafeQuestion(Account a) {
		Req req = ApiUtils.account().post( "/site/updateSafeQuestion" )
				.account( a )
				.header( "Origin", "https://account.bilibili.com" )
				.header( "X-Requested-With", "XMLHttpRequest" )
				.header( "Referer", "https://account.bilibili.com/site" )
				.header( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" )
				.datas( "oldsafequestion", 1, "oldsafeanswer", a.name,
						"newsafequestion", 0, "newsafeanswer", "",
						"change_safe_qa", "false",
						"can_change_safe_qa", 0 );
		return hc.asString( req );
	}

	public Video getVideo(int aid) {
		Req req = ApiUtils.api().get( "/x/video?aid=" + aid );
		JSONObject json = hc.asJSON( req );
		if (json.getIntValue( "code" ) == 0) {
			JSONObject v = json.getJSONObject( "data" );
			v.put( "create", v.getString( "create" ) + ":00" );
			return JSON.toJavaObject( v, Video.class );
		} else
			return null;
	}

	public int addFavorite(Account a, int aid) {
		Req req = ApiUtils.api().get( "/favourite/add?id=" + aid ).account( a );
		String content = hc.asString( req );
		try {
			return JSON.parseObject( content ).getIntValue( "code" );
		} catch (RuntimeException e) {
			Utils.log( content );
			throw e;
		}
	}

	public FavGetList getFavoriteList(Account a) {
		Req req = ApiUtils.space().get( "/ajax/fav/getList" )
				.params(
						"mid", a.mid,
						"fid", a.fid,
						"pagesize", 30,
						"order", "ftime" );
		String content = hc.asString( req );
		JSONObject data = JSON.parseObject( content ).getJSONObject( "data" );
		JSONArray ja = data.getJSONArray( "vlist" );
		for (int i = 0; i < ja.size(); ++i) {
			JSONObject jo = ja.getJSONObject( i );
			jo.put( "create", jo.getString( "create" ) + ":00" );
		}
		return JSON.toJavaObject( data, FavGetList.class );
	}

	public boolean isCommentEmpty(int aid) {
		Req req = ApiUtils.api().get( "/x/reply" )
				.params(
						"type", 1,
						"sort", 0,
						"oid", aid,
						"pn", 1,
						"nohot", 1 );
		JSONObject json = hc.asJSON( req );
		return json.getJSONObject( "data" ).getJSONObject( "page" ).getIntValue( "count" ) == 0;
	}

	public boolean isLogin(Account a) {
		if (a.SESSDATA == null)
			return false;
		Req req = ApiUtils.member().get( "/main.html" ).account( a );
		String content = hc.asString( req );
		return content.contains( Integer.toString( a.mid ) );
	}

	public boolean login(Account a) {
		if (isLogin( a ))
			return true;
		try {
			Req req = ApiUtils.account().post( "/ajax/miniLogin/login" )
					.datas( "userid", a.userid, "pwd", a.password );
			JSONObject json = hc.asJSON( req );
			if (json.getBooleanValue( "status" )) {
				URIBuilder b = null;
				b = new URIBuilder( json.getJSONObject( "data" ).getString( "crossDomain" ) );
				for (NameValuePair nvp : b.getQueryParams()) {
					if (nvp.getName().equals( "DedeUserID" )) {
						a.mid = Integer.parseInt( nvp.getValue() );
					}
					if (nvp.getName().equals( "SESSDATA" )) {
						a.SESSDATA = nvp.getValue();
					}
				}
			}
			return isLogin( a );
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String reply(Account a, int aid, String msg) {
		Req req = ApiUtils.api()
				.post( "/x/reply/add" )
				.account( a )
				.datas( "type", 1, "oid", aid, "message", msg );
		return hc.asString( req );
	}

	public boolean deleteFavoriteList(Account a, FavGetList fvl) {
		if (fvl.vlist.isEmpty())
			return true;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Video v : fvl.vlist) {
			if (!first)
				sb.append( "," );
			first = false;
			sb.append( v.aid );
		}
		String aids = sb.toString();

		Req req = ApiUtils.space().post( "/ajax/fav/mdel" )
				.account( a )
				.datas( "fid", a.fid, "aids", aids )
				.header( "Origin", "http://space.bilibili.com" )
				.header( "Referer", "http://space.bilibili.com/" );
		String content = hc.asString( req );
		try {
			return JSON.parseObject( content ).getBoolean( "status" );
		} catch (RuntimeException e) {
			Utils.log( content );
			throw e;
		}
		//.addHeader( "X-Requested-With", "XMLHttpRequest" )
		//.addHeader( "User-Agent",
		//		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" )
		//.setEntity( HC.makeFormEntity( "fid", a.getFid(), "aids", aids ) )
		//.addParameter( "fid", Integer.toString( a.fid ) ).addParameter( "aids", aids )
		//.addHeader( "Referer", "http://space.bilibili.com/" ).build();
	}

	public void initAccount(Account a) {
		login( a );
		getUserInfo( a );
		initFavBox( a );
	}

	public int addFavorite2(Account a, int aid) {
		Req req = ApiUtils.api()
				.post( "/x/favourite/video/add" )
				.account( a )
				.params( "aid", aid );
		JSONObject json = hc.asJSON( req );
		return json.getIntValue( "code" );
	}

	public FavGetList getFavoriteList2(Account a) {
		Req req = ApiUtils.api()
				.get( "/x/favourite/video" )
				.account( a );
		JSONObject json = hc.asJSON( req );
		JSONObject data = json.getJSONObject( "data" );
		FavGetList ret = new FavGetList();
		ret.count = data.getIntValue( "total" );
		ret.pages = data.getIntValue( "pagecount" );
		JSONArray ja = data.getJSONArray( "videos" );
		if (ja != null)
			for (int i = 0; i < ja.size(); ++i) {
				JSONObject jo = ja.getJSONObject( i );
				jo.put( "create", jo.getString( "create" ) + ":00" );
				ret.vlist.add( JSON.toJavaObject( jo, Video.class ) );
			}
		return ret;
	}

	public boolean deleteFavoriteList2(Account a, FavGetList fvl) {
		for (Video v : fvl.vlist) {
			Req req = ApiUtils.api()
					.post( "/x/favourite/video/del" )
					.account( a )
					.datas( "aid", v.aid );
			String content = hc.asString( req );
		}
		return true;
	}
}
