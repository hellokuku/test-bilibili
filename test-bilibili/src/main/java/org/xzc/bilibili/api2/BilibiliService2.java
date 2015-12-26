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
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.xzc.bilibili.api2.reply.Reply;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.scan.Page;
import org.xzc.http.HC;
import org.xzc.http.Params;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

public class BilibiliService2 {
	private static final Logger log = Logger.getLogger( BilibiliService2.class );
	public static final String API_HOST = "api.bilibili.com";
	public static final String API_IP = "61.164.47.167";
	private String apiServerIP = API_IP;

	public static final String ACCOUNT_HOST = "account.bilibili.com";
	public static final String ACCOUNT_IP = "61.164.47.167";
	private String accountServerIP = ACCOUNT_IP;

	public static final String MEMBER_HOST = "member.bilibili.com";
	public static final String MEMBER_IP = "61.164.47.167";
	private String memberServerIP = MEMBER_IP;

	public BilibiliService2() {
	}

	public HC getHC() {
		return hc;
	}

	@PostConstruct
	public void postConstruct() {
		int timeout = 15000;
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
		CloseableHttpClient chc = HttpClients.custom()
				.setProxy( proxy )
				.addInterceptorFirst( new HttpRequestInterceptor() {
					public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
						request.addHeader( "Cookie",
								"DedeUserID=" + DedeUserID + "; SESSDATA=" + SESSDATA + "; DedeID=" + DedeID + ";" );
					}
				} ).setDefaultRequestConfig( rc ).build();
		hc = new HC( chc );
	}

	@PreDestroy
	public void preDestroy() {
	}

	private HC hc;

	private String DedeUserID;
	private String DedeID;

	public String getDedeID() {
		return DedeID;
	}

	public void setDedeID(String dedeID) {
		DedeID = dedeID;
	}

	private String SESSDATA;
	private String proxyHost;
	private int proxyPort;

	public void setDedeUserID(String dedeUserID) {
		DedeUserID = dedeUserID;
	}

	public void setSESSDATA(String sESSDATA) {
		SESSDATA = sESSDATA;
	}

	public String getDedeUserID() {
		return DedeUserID;
	}

	public String getSESSDATA() {
		return SESSDATA;
	}

	public boolean login(String userid, String pwd) {
		try {
			Req req = Req.post( "http://" + accountServerIP + "/ajax/miniLogin/login" )
					.setHost( ACCOUNT_HOST )
					.setDatas( new Params( "userid", userid, "pwd", pwd ) );
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

	public boolean isLogined() {
		if (DedeUserID == null || SESSDATA == null)
			return false;
		String content = hc.asString( Req.get( "http://" + memberServerIP + "/main.html" )
				.setHost( MEMBER_HOST ) );
		return content.contains( DedeUserID );
	}

	public Result<Video> getVideo(int aid) {
		String content = hc.asString( Req.get( "http://" + apiServerIP + "/x/video?aid=" + aid ).setHost( API_HOST ) );
		JSONObject json = JSON.parseObject( content );
		int code = json.getIntValue( "code" );
		if (code != 0) {
			return new Result<Video>( false, code, code, "视频不存在", content, null );
		}
		return new Result<Video>( true, JSON.toJavaObject( json, Video.class ) );
	}

	public Result<Page<Reply>> getReplyList(int aid, int page) {
		String content = hc.asString(
				Req.get( "http://" + apiServerIP + "/x/reply?type=1&sort=0&nohot=1&pn=" + page + "&oid=" + aid )
						.setHost( API_HOST ) );
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

	public boolean shareFirst() {
		Req req = Req.post( "http://" + apiServerIP + "/x/share/first" )
				.setHost( API_HOST )
				.setDatas( "id", DedeID, "type", 0, "jsonp", "json" );
		String content = hc.asString( req );
		JSONObject json = JSON.parseObject( content );
		return json.getIntValue( "code" ) == 0;
	}

	public boolean reportWatch() {
		Req req = Req.get(
				"http://www.bilibili.com/api_proxy?app=bangumi&action=/report_watch&oid=" + DedeID + "&aid=" + DedeID );
		String content = hc.asString( req );
		return JSON.parseObject( content ).getIntValue( "code" ) == 0;
	}

	public Account getUserInfo() {

		Req req = Req.get( "http://" + API_IP + "/myinfo" ).setHost( API_HOST );
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

	public void other() {
		hc.getAsString( "http://www.bilibili.com/" );
		hc.getAsString( "http://www.bilibili.com/video/av3431726/" );
	}

	public void clear() {
		DedeID = null;
		DedeUserID = null;
		SESSDATA = null;
	}

	public void setProxy(String proxyHost, int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}

	public String report(int aid, int rpid, int reason, String content) {
		Req req = Req.post( "http://" + API_IP + "/x/reply/report?jsonp=jsonp" )
				.setHost( API_HOST )
				.setDatas( "oid", aid, "type", 1, "rpid", rpid, "reason", reason, "content", content );
		return hc.asString( req );
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

	public String action(int aid, int rpid, int action) {
		Req req = Req.post( "http://api.bilibili.com/x/reply/action" )
				.setDatas( "jsonp", "jsonp", "oid", aid, "type", 1, "rpid", rpid, "action", action );
		return hc.asString( req );
	}

}
