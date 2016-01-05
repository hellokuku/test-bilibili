package org.xzc.bilibili.scan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Result;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.util.Utils;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BilibiliService {
	public static final String DEFAULT_IP = "218.205.74.9";
	public static final String SPACE_IP = DEFAULT_IP;
	public static final String SPACE_HOST = "space.bilibili.com";
	public static final String API_IP =DEFAULT_IP;
	public static final String API_HOST = "api.bilibili.com";
	public static final String INTERFACE_IP = DEFAULT_IP;
	public static final String INTERFACE_HOST = "interface.bilibili.com";
	public static final String MEMBER_IP = DEFAULT_IP;
	public static final String MEMBER_HOST = "member.bilibili.com";
	public static final String ACCOUNT_HOST = "account.bilibili.com";
	public static final String ACCOUNT_IP = DEFAULT_IP;

	private static Pattern RESULT_PATTERN = Pattern.compile( "abc\\(\"(.+)\"\\)" );

	/**
	 * 生成aids
	 * @param json
	 * @return
	 */
	private static String makeDeletedAids(FavGetList json) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Video v : json.vlist) {
			if (!first)
				sb.append( "," );
			first = false;
			sb.append( v.aid );
		}
		return sb.toString();
	}

	/**
	 * 绑定的hc
	 */
	private HC hc;

	/**
	 * 绑定的账号
	 */
	private Account a;

	public BilibiliService(final Account account, String proxyHost, int proxyPort) {
		this.a = account;

		//控制连接并发量
		PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
		m.setDefaultMaxPerRoute( 4 );
		m.setMaxTotal( 20 );

		//忽略cookie
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		HttpHost proxy = null;
		if (proxyHost != null) {
			proxy = new HttpHost( proxyHost, proxyPort );
		}
		CloseableHttpClient chc = HttpClients.custom()
				.setProxy( proxy )
				.setDefaultRequestConfig( rc )
				.addInterceptorFirst( new HttpRequestInterceptor() {
					public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
						request.addHeader( "Cookie",
								"DedeUserID=" + a.mid + "; SESSDATA=" + a.SESSDATA + ";" );
					}
				} ).setConnectionManager( m ).build();

		hc = new HC( chc );
	}

	public BilibiliService(final Account account) {
		this( account, null, 0 );
	}

	@PostConstruct
	public void postConstruct() {
		initAccount();
	}

	/**
	 * 添加视频到收藏夹
	 * 0成功 11007 重复 -1111不存在
	 * @param aid
	 * @return
	 */
	public int addFavotite(final int aid) {
		HttpUriRequest req = RequestBuilder.get( "http://" + API_IP + "/favourite/add?id=" + aid )
				.addHeader( "Host", API_HOST )
				.build();
		String content = hc.asString( req );
		try {
			return JSON.parseObject( content ).getIntValue( "code" );
		} catch (RuntimeException e) {
			Utils.log( content );
			throw (RuntimeException) e;
		}
	}

	/**
	 * 进行评论
	 * @param aid
	 * @param msg
	 */
	public Result comment(final int aid, final String msg) {
		HttpUriRequest req = makeCommentRequest( aid, msg );
		String content = hc.asString( req );
		Matcher matcher = RESULT_PATTERN.matcher( content );
		matcher.find();
		String s = Utils.decodeUnicode( matcher.group( 1 ) );
		return new Result( "OK".equals( s ), s );
	}

	public Result deleteFavoriteJSON(FavGetList fgl) {
		if (fgl.vlist.size() == 0) {
			return new Result( true, "要删除的视频id为空, 不发请求." );
		}
		String aids = makeDeletedAids( fgl );
		HttpUriRequest req = makeDeleteFavoriteRequest( aids );
		String result = hc.asString( req );
		JSONObject json = JSON.parseObject( result );
		return new Result( json.getBooleanValue( "status" ), json.getString( "data" ) );
	}

	public FavGetList getFavoriteListJSON(final int pagesize) {
		HttpUriRequest req = makeGetFavoriteListRequest( pagesize );
		String content = hc.asString( req );
		try {
			JSONObject data = JSON.parseObject( content ).getJSONObject( "data" );
			JSONArray ja = data.getJSONArray( "vlist" );
			//这里修正一下时间的格式
			for (int i = 0; i < ja.size(); ++i) {
				JSONObject jo = ja.getJSONObject( i );
				jo.put( "create", jo.getString( "create" ) + ":00" );
			}
			FavGetList fgl = JSON.toJavaObject( data, FavGetList.class );
			return fgl;
		} catch (Exception e) {
			Utils.log( e.getMessage() );
			throw new RuntimeException( e );
		}
	}

	/**
	 * 获取评论
	 * @return
	 */
	private HttpUriRequest makeCommentListRequest(int aid, int page, int pagesize) {
		return RequestBuilder.get( "http://" + API_IP + "/feedback" ).addParameter( "page", Integer.toString( page ) )
				.addHeader( "Host", API_HOST )
				.addParameter( "mode", "arc" ).addParameter( "type", "json" ).addParameter( "ver", "3" )
				.addParameter( "aid", Integer.toString( aid ) ).addParameter( "order", "default" )
				.addParameter( "pagesize", Integer.toString( pagesize ) ).build();
	}

	/**
	 * 评论列表是否为空
	 * @param aid
	 * @return
	 */
	public boolean isCommentListEmpty(int aid) {
		HttpUriRequest req = makeCommentListRequest( aid, 1, 1 );
		JSONObject json = hc.asJSON( req );
		return json.getIntValue( "results" ) == 0;
	}

	public boolean isLogin() {
		HttpUriRequest req = RequestBuilder.get( "http://" + MEMBER_IP + "/main.html" )
				.addHeader( "Host", MEMBER_HOST )
				.build();
		String content = hc.asString( req );
		return content.contains( Integer.toString( a.mid ) );
	}

	private boolean initAccount() {
		HttpUriRequest req = RequestBuilder.get( "http://" + MEMBER_IP + "/main.html" )
				.addHeader( "Host", MEMBER_HOST )
				.build();
		String content = hc.asString( req );
		if (!content.contains( Integer.toString( a.mid ) ))//账号还没有登陆
			return false;
		req = RequestBuilder.get( "http://" + API_IP + "/myinfo" )
				.addHeader( "Host", API_HOST )
				.build();
		String jsonStr = hc.asString( req );
		Account aa = JSON.parseObject( jsonStr, Account.class );
		aa.SESSDATA = a.SESSDATA;
		aa.mid = a.mid;

		a = aa;

		//获得默认的收藏夹
		req = RequestBuilder.get( "http://" + SPACE_IP + "/ajax/fav/getBoxList?mid=" + a.mid )
				.addHeader( "Host", SPACE_HOST ).build();
		JSONObject jo = hc.asJSON( req );
		a.fid = jo.getJSONObject( "data" ).getJSONArray( "list" ).getJSONObject( 0 ).getIntValue( "fav_box" );
		System.out.println( "初始化账号成功" + a );
		return true;
	}

	/**
	 * 进行评论
	 * @param aid
	 * @param msg
	 * @return
	 */
	private HttpUriRequest makeCommentRequest(int aid, String msg) {
		return RequestBuilder.get( "http://" + INTERFACE_IP + "/feedback/post" )
				.addHeader( "Host", INTERFACE_HOST )
				.addParameter( "callback", "abc" )
				.addParameter( "aid", Integer.toString( aid ) )
				.addParameter( "msg", msg )
				.addParameter( "action", "send" )
				.addHeader( "Referer", "http://www.bilibili.com/video/av" + aid )
				.build();
	}

	/**
	 * 从默认收藏夹中删除这些aid
	 * @param aids
	 * @return
	 */
	private HttpUriRequest makeDeleteFavoriteRequest(String aids) {
		return RequestBuilder.post( "http://" + SPACE_IP + "/ajax/fav/mdel" )
				.addHeader( "Host", SPACE_HOST )
				.addHeader( "Origin", "http://space.bilibili.com" )
				.addHeader( "X-Requested-With", "XMLHttpRequest" )
				.addHeader( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" )
				//.setEntity( HC.makeFormEntity( "fid", a.getFid(), "aids", aids ) )
				.addParameter( "fid", Integer.toString( a.fid ) ).addParameter( "aids", aids )
				.addHeader( "Referer", "http://space.bilibili.com/" ).build();
	}

	/**
	 * 获得默认收藏夹的内容
	 * @param pagesize
	 * @return
	 */
	private HttpUriRequest makeGetFavoriteListRequest(int pagesize) {
		return RequestBuilder.get( "http://" + SPACE_IP + "/ajax/fav/getList" )
				.addHeader( "Host", SPACE_HOST )
				.addParameter( "mid", Integer.toString( a.mid ) )
				.addParameter( "fid", Integer.toString( a.fid ) )
				.addParameter( "pagesize", Integer.toString( pagesize ) ).addParameter( "order", "ftime" ).build();
	}

	@PreDestroy
	public void close() {
		hc.close();
	}

	/**
	 * 消耗掉默认收藏夹的所有内容
	 * @return
	 */
	public FavGetList consumeAllFavoriteListJSON() {
		FavGetList ret = new FavGetList();
		List<Video> vlist = new ArrayList<Video>();
		while (true) {
			FavGetList fgl = getFavoriteListJSON( 50 );
			Result r = deleteFavoriteJSON( fgl );
			if (!r.success) {
				throw new RuntimeException( a + " 删除收藏夹失败, 请检查cookie! " + r );
			}
			ret.count += fgl.count;
			vlist.addAll( fgl.vlist );
			if (fgl.count == fgl.vlist.size())
				break;
		}
		ret.pages = 1;
		ret.vlist = vlist;
		return ret;
	}

	public Account getAccount() {
		return a;
	}

	public boolean login() {
		try {
			Req req = Req.post( "http://" + ACCOUNT_IP + "/ajax/miniLogin/login" )
					.host( ACCOUNT_HOST )
					.datas( "userid", a.userid, "pwd", a.password );
			String content = hc.asString( req );
			JSONObject json = JSON.parseObject( content );
			if (json.getBooleanValue( "status" )) {
				URIBuilder b = null;
				b = new URIBuilder( json.getJSONObject( "data" ).getString( "crossDomain" ) );
				for (NameValuePair nvp : b.getQueryParams()) {
					if (nvp.getName().equals( "DedeUserID" ))
						a.mid = Integer.parseInt( nvp.getValue() );
					if (nvp.getName().equals( "SESSDATA" ))
						a.SESSDATA = nvp.getValue();
				}
				return true;
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}
}
