package org.xzc.bilibili.api2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.xzc.bilibili.api2.reply.Reply;
import org.xzc.bilibili.autosignin.ExpState;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.scan.Page;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

public class BilibiliService2 {
	private static final Logger log = Logger.getLogger( BilibiliService2.class );

	//如果为true的话 会自动尝试加入cookie
	private boolean autoCookie = true;

	private HC hc;

	private String DedeUserID;

	private String DedeID;

	private String SESSDATA;

	private String proxyHost;

	private int proxyPort;

	private int batch = 2;

	public BilibiliService2() {
	}

	public String action(int aid, int rpid, int action) {
		Req req = Req.post( "http://api.bilibili.com/x/reply/action" )
				.datas( "jsonp", "jsonp", "oid", aid, "type", 1, "rpid", rpid, "action", action );
		return hc.asString( req );
	}

	public void clear() {
		DedeID = null;
		DedeUserID = null;
		SESSDATA = null;
	}

	public int getBatch() {
		return batch;
	}

	public String getDedeID() {
		return DedeID;
	}

	public String getDedeUserID() {
		return DedeUserID;
	}

	public HC getHC() {
		return hc;
	}

	public Result<Page<Reply>> getReplyList(int aid, int page) {
		String content = hc.asString(
				Req.get( "http://" + ApiUtils.API_IP + "/x/reply?type=1&sort=0&nohot=1&pn=" + page + "&oid=" + aid )
						.host( ApiUtils.API_HOST ) );
		JSONObject json = JSON.parseObject( content );
		int code = json.getIntValue( "code" );
		if (code != 0) {
			return new Result<Page<Reply>>( false, code, code, "视频不存在", content, null );
		}
		Page<Reply> ret = new Page<Reply>();
		ret.list = JSON.toJavaObject( json.getJSONObject( "data" ).getJSONArray( "" ),
				(Class<List<Reply>>) new TypeReference<List<Reply>>() {
				}.getType() );
		ret.pagesize = 20;
		ret.page = page;
		ret.total = json.getJSONObject( "data" ).getJSONObject( "page" ).getIntValue( "count" );
		return new Result<Page<Reply>>( true, ret );
	}

	public String getSESSDATA() {
		return SESSDATA;
	}

	public Account getUserInfo() {
		return getUserInfo( (Account) null );
	}

	public Account getUserInfo(Account a) {
		return getUserInfo( Req.get( "http://" + ApiUtils.API_IP + "/myinfo" ).host( ApiUtils.API_HOST ).account( a ) );
	}

	public Result<Video> getVideo(int aid) {
		String content = hc.asString( Req.get( "http://" + ApiUtils.API_IP+ "/x/video?aid=" + aid ).host( ApiUtils.API_HOST ) );
		JSONObject json = JSON.parseObject( content );
		int code = json.getIntValue( "code" );
		if (code != 0) {
			return new Result<Video>( false, code, code, "视频不存在", content, null );
		}
		return new Result<Video>( true, JSON.toJavaObject( json, Video.class ) );
	}

	public boolean isAutoCookie() {
		return autoCookie;
	}

	public boolean isLogined() {
		if (DedeUserID == null || SESSDATA == null)
			return false;
		String content = hc.asString( Req.get( "http://" + ApiUtils.MEMBER_IP+ "/main.html" )
				.host( ApiUtils.MEMBER_HOST ) );
		return content.contains( DedeUserID );
	}

	public boolean login(Account a) {
		if (a.SESSDATA != null) {
			DedeUserID = Integer.toString( a.mid );
			SESSDATA = a.SESSDATA;
			return isLogined();
		} else {
			return login( a.userid, a.password );
		}
	}

	public boolean login(String userid, String pwd) {
		try {
			Req req = Req.post( "http://" + ApiUtils.ACCOUNT_IP+ "/ajax/miniLogin/login" )
					.host( ApiUtils.ACCOUNT_HOST )
					.datas( "userid", userid, "pwd", pwd );
			String content = hc.asString( req );
			JSONObject json = JSON.parseObject( content );
			if (json.getBooleanValue( "status" )) {
				URIBuilder b = null;
				b = new URIBuilder( json.getJSONObject( "data" ).getString( "crossDomain" ) );
				for (NameValuePair nvp : b.getQueryParams()) {
					if (nvp.getName().equals( "DedeUserID" ))
						DedeUserID = nvp.getValue();
					if (nvp.getName().equals( "SESSDATA" ))
						SESSDATA = nvp.getValue();
				}
				return true;
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void login0(Account a) {
		if (a.SESSDATA == null) {
			login( a );
		} else {
			SESSDATA = a.SESSDATA;
			DedeUserID = Integer.toString( a.mid );
		}
	}

	public void other() {
		//JSONObject json = hc.getAsJSON( "https://account.bilibili.com/site/GetExpLog" );
		//System.out.println( json.getJSONObject( "data" ).getJSONArray( "result" ).getJSONObject( 0 ) );
		String mid = DedeUserID;
		hc.getAsString( "http://www.bilibili.com/" );
		hc.getAsString( "http://www.bilibili.com/video/av3471617/" );
		hc.getAsString( "http://www.bilibili.com/api_proxy?app=tag&action=/tags/archive_list&aid=3471617&nomid=1" );
		//hc.getAsString( "http://www.bilibili.com/api_proxy?app=tag&action=/tags/subscribe_list" );
		/*hc.getAsString( "http://data.bilibili.com/v/web/web_page_view?mid=" + mid
				+ "&fts=1451531556&url=http%253A%252F%252Fwww.bilibili.com%252Fvideo%252Fav3471617%252F&proid=1&ptype=1&other=&module=main&title=%25E4%25B9%2590%25E6%25AD%25A3%25E7%25BB%25AB%25E7%259A%2584%25E5%258D%2583%25E6%259C%25AC%25E6%25A8%25B1%25EF%25BC%2588%25E4%25B8%25AD%25E5%259B%25BD%25E9%2580%259A%25E5%258F%25B2%25E7%2589%2588%25E6%25AD%258C%25E8%25AF%258D%25EF%25BC%2589_MMD%25C2%25B73D_%25E5%258A%25A8%25E7%2594%25BB_bilibili_%25E5%2593%2594%25E5%2593%25A9%25E5%2593%2594%25E5%2593%25A9%25E5%25BC%25B9%25E5%25B9%2595%25E8%25A7%2586%25E9%25A2%2591%25E7%25BD%2591&ajaxtag=&ajaxid=&_=1451531555900" );
		hc.getAsString( "http://data.bilibili.com/v/web/home_pic?mid=" + mid
				+ "&fts=1451531556&url=http%253A%252F%252Fwww.bilibili.com%252Fvideo%252Fav3471617%252F&proid=1&ptype=1&other=&load=1&detail=&_=1451531555907" );
		hc.getAsString( "http://data.bilibili.com/v/web/homepage_navigation?mid=" + mid
				+ "&fts=1451531556&url=http%253A%252F%252Fwww.bilibili.com%252Fvideo%252Fav3471617%252F&proid=1&ptype=1&other=&optype=1&clickitem=&pagetype=13&_=1451531555913" );
		hc.getAsString( "http://data.bilibili.com/v/web/web_underplayer_toolbar?mid=" + mid
				+ "&fts=1451531556&url=http%253A%252F%252Fwww.bilibili.com%252Fvideo%252Fav3471617%252F&proid=1&ptype=1&other=&optype=2&clickid=&showid=4&_=1451531555918" );
		hc.getAsString( "http://data.bilibili.com/v/web/home_tag_list_show?mid=" + mid
				+ "&fts=1451531556&url=http%253A%252F%252Fwww.bilibili.com%252Fvideo%252Fav3471617%252F&proid=1&ptype=1&other=&pageid=3471617&pagetype=1&result=3&_=1451531555926" );
		hc.getAsString( "http://www.bilibili.com/plus/widget/ajaxGetCaptchaKey.php?js&_=1451531556106" );
		
		hc.getAsString(
				"http://interface.bilibili.com/count?key=baac268505a2e69e3c1e8f32&nr=1&aid=3471617&mid=892086&_=1451531555239" );
		*/
		hc.getAsString( "http://interface.bilibili.com/player?id=cid:5514894&aid=3471617" );
		/*
		hc.getAsString( "http://data.bilibili.com/v/flashplay/flash_player_play?mid=" + DedeUserID
				+ "&fid=web%5Fplayer&title=%E4%B9%90%E6%AD%A3%E7%BB%AB%E7%9A%84%E5%8D%83%E6%9C%AC%E6%A8%B1%EF%BC%88%E4%B8%AD%E5%9B%BD%E9%80%9A%E5%8F%B2%E7%89%88%E6%AD%8C%E8%AF%8D%EF%BC%89&fver=20150901&avid=3471617&cid=5514894&pname=1&loop=2&quality=2" );
		*/
		//json = hc.getAsJSON( "https://account.bilibili.com/site/GetExpLog" );
		//System.out.println( json.getJSONObject( "data" ).getJSONArray( "result" ).getJSONObject( 0 ) );
	}

	private int timeout = 15000;

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@PostConstruct
	public void postConstruct() {
		RequestConfig rc = RequestConfig.custom()
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout )
				.build();
		HttpHost proxy = null;
		if (proxyHost != null) {
			proxy = new HttpHost( proxyHost, proxyPort );
		}
		PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
		m.setMaxTotal( batch * 2 );
		m.setDefaultMaxPerRoute( batch );
		CloseableHttpClient chc = HttpClients.custom()
				.setProxy( proxy )
				.setConnectionManager( m )
				.addInterceptorFirst( new HttpRequestInterceptor() {
					public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
						if (request.getFirstHeader( "account" ) == null && autoCookie)
							request.addHeader( "Cookie",
									"DedeUserID=" + DedeUserID + "; SESSDATA=" + SESSDATA + "; DedeID=" + DedeID
											+ ";" );
					}
				} ).setDefaultRequestConfig( rc ).build();
		hc = new HC( chc );
	}

	@PreDestroy
	public void preDestroy() {
	}

	public String report(Account a, int aid, int rpid, int reason, String content) {
		Req req = Req.post( "http://" + ApiUtils.API_IP + "/x/reply/report?jsonp=jsonp" )
				.host( ApiUtils.API_HOST )
				.datas( "oid", aid, "type", 1, "rpid", rpid, "reason", reason, "content", content )
				.account( a );
		return hc.asString( req );
	}

	public boolean reportWatch() {
		Req req = Req.get(
				"http://www.bilibili.com/api_proxy?action=/report_watch&oid=" + DedeID + "&aid=" + DedeID );
		String content = hc.asString( req );
		return JSON.parseObject( content ).getIntValue( "code" ) == 0;
	}

	public void setAutoCookie(boolean autoCookie) {
		this.autoCookie = autoCookie;
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

	public void setDedeID(String dedeID) {
		DedeID = dedeID;
	}

	public void setProxy(String proxyHost, int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	public boolean shareFirst() {
		return shareFirst( (Account) null );
	}

	public boolean shareFirst(Account a) {
		return shareFirst( Req.post( "http://" + ApiUtils.API_IP+ "/x/share/first" )
				.host( ApiUtils.API_HOST )
				.datas( "id", DedeID, "type", 0, "jsonp", "json" )
				.account( a ) );
	}

	public String updatePwd(Account a, String newPassword) {
		return updatePwd( a, a.password, newPassword );
	}

	public String updatePwd(Account a, String oldPassword, String newPassword) {
		Req req = Req.post( "https://account.bilibili.com/site/updatePwd" )
				.datas( "oldpwd", oldPassword, "userpwd", newPassword, "userpwdok", newPassword, "safequestion", 0,
						"safeanswer", "" )
				.header( "Referer", "https://account.bilibili.com/" )
				.header( "Origin", "https://account.bilibili.com" )
				.header( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" )
				.header( "X-Requested-With", "XMLHttpRequest" ).account( a );
		return updatePwd( req );
	}

	public String updatePwd(String oldPassword, String newPassword) {
		return updatePwd( (Account) null, oldPassword, newPassword );
	}

	private Account getUserInfo(Req req) {
		JSONObject json = hc.asJSON( req );
		Account a = JSON.toJavaObject( json, Account.class );
		//等级信息
		JSONObject level_info = json.getJSONObject( "level_info" );
		a.currentLevel = level_info.getIntValue( "current_level" );
		a.currentMin = level_info.getIntValue( "current_min" );
		a.currentExp = level_info.getIntValue( "current_exp" );
		a.nextExp = level_info.getIntValue( "next_exp" );
		//cookie和账号密码信息
		a.SESSDATA = SESSDATA;
		return a;
	}

	private boolean shareFirst(Req req) {
		String content = hc.asString( req );
		JSONObject json = JSON.parseObject( content );
		return json.getIntValue( "code" ) == 0;
	}

	private String updatePwd(Req req) {
		return hc.asString( req );
	}

	public ExpState getExpState() {
		String content = hc.getAsString( "https://account.bilibili.com/site/GetExpLog" );
		JSONObject json = json = JSON.parseObject( content );
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
}
