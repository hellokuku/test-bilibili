package org.xzc.bilibili.api2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.xzc.bilibili.api.Params;
import org.xzc.bilibili.api2.reply.Reply;
import org.xzc.bilibili.autosignin.AccountForAutoSignIn;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.scan.Page;
import org.xzc.bilibili.util.HC;

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

	@PostConstruct
	public void postConstruct() {
		int timeout = 15000;
		RequestConfig rc = RequestConfig.custom()
				.setCookieSpec( CookieSpecs.IGNORE_COOKIES )
				.setConnectionRequestTimeout( timeout )
				.setConnectTimeout( timeout )
				.setSocketTimeout( timeout )
				.build();
		CloseableHttpClient chc = HttpClients.custom().addInterceptorFirst( new HttpRequestInterceptor() {
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				request.addHeader( "Cookie", "DedeUserID=" + DedeUserID + "; SESSDATA=" + SESSDATA + ";" );
			}
		} ).setDefaultRequestConfig( rc ).build();
		hc = new HC( chc );
	}

	@PreDestroy
	public void preDestroy() {

	}

	private HC hc;

	private String DedeUserID;
	private String SESSDATA;

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
			HttpUriRequest req = RequestBuilder.post( "http://" + accountServerIP + "/ajax/miniLogin/login" )
					.setEntity( new Params( "userid", userid, "pwd", pwd ).toEntity() )
					.addHeader( "Host", ACCOUNT_HOST )
					.build();
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
		String content = hc.asString( RequestBuilder.get( "http://" + memberServerIP + "/main.html" )
				.addHeader( "Host", MEMBER_HOST ).build() );
		return content.contains( DedeUserID );
	}

	public Result<Video> getVideo(int aid) {
		HttpUriRequest req = RequestBuilder.get( "http://" + apiServerIP + "/x/video?aid=" + aid )
				.addHeader( "Host", API_HOST ).build();
		String content = hc.asString( req );
		JSONObject json = JSON.parseObject( content );
		int code = json.getIntValue( "code" );
		if (code != 0) {
			return new Result<Video>( false, code, code, "视频不存在", content, null );
		}
		return new Result<Video>( true, JSON.toJavaObject( json, Video.class ) );
	}

	public Result<Page<Reply>> getReplyList(int aid, int page) {
		//http://api.bilibili.com/x/reply?type=1&sort=0&nohot=1&pn=1&oid=3440187
		HttpUriRequest req = RequestBuilder
				.get( "http://" + apiServerIP + "/x/reply?type=1&sort=0&nohot=1&pn=" + page + "&oid=" + aid )
				.addHeader( "Host", API_HOST ).build();
		String content = hc.asString( req );
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

	public boolean shareFirst(int aid) {
		HttpUriRequest req = RequestBuilder.post( "http://" + apiServerIP + "/x/share/first" )
				.addHeader( "Host", API_HOST )
				.setEntity( new Params(
						"id", 45229,
						"typeid", 20,
						"type", 1 )
								.toEntity() )
				.build();
		String content = hc.asString( req );
		if (log.isDebugEnabled())
			log.debug( content );
		JSONObject json = JSON.parseObject( content );
		return json.getIntValue( "code" ) == 0;
	}

	public boolean reportWatch() {
		//http://www.bilibili.com/api_proxy?app=bangumi&action=/report_watch&episode_id=81849
		HttpUriRequest req = RequestBuilder
				.get( "http://www.bilibili.com/api_proxy?app=bangumi&action=/report_watch&episode_id=81849" ).build();
		String content = hc.asString( req );
		return JSON.parseObject( content ).getIntValue( "code" ) == 0;
	}

	public AccountForAutoSignIn getUserInfo() {
		JSONObject json = hc.getAsJSON( "http://api.bilibili.com/myinfo" );
		AccountForAutoSignIn a = JSON.toJavaObject( json, AccountForAutoSignIn.class );
		JSONObject level_info = json.getJSONObject( "level_info" );
		a.currentLevel = level_info.getIntValue( "current_level" );
		a.currentMin = level_info.getIntValue( "current_min" );
		a.currentExp = level_info.getIntValue( "current_exp" );
		a.nextExp = level_info.getIntValue( "next_exp" );
		return a;
	}

}
