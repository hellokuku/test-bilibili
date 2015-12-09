package org.xzc.bilibili;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.Bangumi;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.model.json.FavGetList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;

public class BilibiliService {
	private static final Charset UTF8 = Charset.forName( "utf8" );
	private static Pattern RESULT_PATTERN = Pattern.compile( "abc\\(\"(.+)\"\\)" );
	private static final String FAV_GET_BOX_LIST_URL = "http://space.bilibili.com/ajax/fav/getBoxList?mid=";

	private static String getDeleteAids(FavGetList json) {
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
	 * 制作一个cookie
	 * @param name
	 * @param value
	 * @return
	 */
	private static Cookie makeCookie(String name, String value) {
		BasicClientCookie c = new BasicClientCookie( name, value );
		Calendar calendar = Calendar.getInstance();
		calendar.set( Calendar.YEAR, 2020 );
		c.setDomain( ".bilibili.com" );
		c.setPath( "/" );
		c.setExpiryDate( calendar.getTime() );
		c.setSecure( false );
		c.setVersion( 1 );
		return c;
	}

	/**
	 * 绑定的hc
	 */
	private CloseableHttpClient hc;

	/**
	 * 绑定的账号
	 */
	private Account a;

	private BasicCookieStore makeCookieStore() {
		BasicCookieStore bcs = new BasicCookieStore();
		bcs.addCookie( makeCookie( "DedeUserID", Integer.toString( a.id ) ) );
		bcs.addCookie( makeCookie( "SESSDATA", a.SESSIDATA ) );
		return bcs;
	}

	public BilibiliService(Account a) {
		this.a = a;
		//控制连接并发量
		PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
		m.setDefaultMaxPerRoute( 4 );
		m.setMaxTotal( 20 );

		hc = HttpClients.custom().setConnectionManager( m ).build();
		rebuildContext();
	}

	public synchronized void rebuildContext() {
		boolean ok=false;
		for(int i=0;i<10;++i){
			if(rebuildContextInternal()){
				ok=true;
				break;
			}
		}
		if (!ok) 
			throw new RuntimeException( "尽力了,但是initAccount失败." );
	}

	private boolean rebuildContextInternal() {
		BasicCookieStore bcs = makeCookieStore();
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.NETSCAPE ).build();
		HttpClientContext ctx2 = new HttpClientContext();
		ctx2.setCookieStore( bcs );
		ctx2.setRequestConfig( rc );
		ctx = new BasicHttpContext( ctx2 );

		return initAccount();
	}

	private BasicHttpContext ctx;

	private String lastFavoriteContent;
	
	public String getLastFavoriteContent() {
		return lastFavoriteContent;
	}

	/**
	 * 添加视频到收藏夹
	 * 0成功 11007 重复 -1111不存在
	 * @param aid
	 * @return
	 */
	public int addFavotite(final int aid) {
		return safeRun( new SafeRunner<Integer>() {
			public Integer run() throws Exception {
				String url = "http://api.bilibili.com/favourite/add?id=" + aid;
				String content = asString( url );
				lastFavoriteContent=content;
				return JSON.parseObject( content ).getIntValue( "code" );
			}
		} );
	}

	/**
	 * 进行评论
	 * @param aid
	 * @param msg
	 */
	public String comment(final int aid, final String msg) {
		HttpUriRequest req = makeCommentRequest( aid, msg );
		String content = asString( req );
		Matcher matcher = RESULT_PATTERN.matcher( content );
		matcher.find();
		return Utils.decodeUnicode( matcher.group( 1 ) );
	}

	public void danmu() {
		String url = "http://interface.bilibili.com/dmpost?cid=3374460&aid=2168384&pid=1";
		/*
		List<NameValuePair> formData = new ArrayList<NameValuePair>();
		formData.add( new BasicNameValuePair( "color", "16777215" ) );
		formData.add( new BasicNameValuePair( "date", "2015-12-07 17:03:10" ) );
		formData.add( new BasicNameValuePair( "cid", "3374460" ) );
		formData.add( new BasicNameValuePair( "pool", "0" ) );
		formData.add( new BasicNameValuePair( "mode", "1" ) );
		formData.add( new BasicNameValuePair( "message", "求求第三季啊, 一定要出." ) );
		formData.add( new BasicNameValuePair( "rnd", "761437893" ) );
		formData.add( new BasicNameValuePair( "playTime", "628.993" ) );
		formData.add( new BasicNameValuePair( "fontsize", "25" ) );
		UrlEncodedFormEntity e = null;
		try {
			e = new UrlEncodedFormEntity( formData );
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}*/
		HttpUriRequest req = RequestBuilder.post( url ).addParameter( "color", "16777215" )
				.addParameter( "date", "2015-12-07 16:48:10" ).addParameter( "cid", "3374460" )
				.addParameter( "pool", "0" ).addParameter( "mode", "1" ).addParameter( "message", "求第三季啊, 一定要出." )
				.addParameter( "rnd", "761437893" ).addParameter( "playTime", "688.993" )
				.addParameter( "fontsize", "25" ).addHeader( "Origin", "http://static.hdslb.com" )
				.addHeader( "X-Requested-With", "ShockwaveFlash/19.9.9.999" )
				.addHeader( "Referer", "http://static.hdslb.com/play.swf" )
				//.setEntity( e )
				.build();
		String res = asString( req );
		System.out.println( res );
		//应该是期待返回一个时间戳
	}

	public String deleteFavorite(int... aids) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < aids.length; ++i) {
			if (i > 0)
				sb.append( "," );
			sb.append( aids[i] );
		}
		return deleteFavorite( sb.toString() );
	}

	public String deleteFavorite(String aids) {
		HttpUriRequest req = makeDeleteFavoriteRequest( aids );
		String content = asString( req );
		return content;
	}

	public String deleteFavorite2(String aids) {
		HttpUriRequest req = RequestBuilder.post( "http://space.bilibili.com/ajax/fav/mdel" )
				.addParameter( "fid", Integer.toString( a.fid ) ).addParameter( "aids", aids )
				.addHeader( "Referer", "http://space.bilibili.com/" ).build();
		String content = asString( req );
		return content;
	}

	public boolean deleteFavoriteJSON(FavGetList json) {
		String aids = getDeleteAids( json );
		HttpUriRequest req = makeDeleteFavoriteRequest( aids );
		return asJSON( req ).getBooleanValue( "status" );
	}

	public Bangumi getBangumi(final String bid) {
		return safeRun( new SafeRunner<Bangumi>() {
			public Bangumi run() throws Exception {
				String url = "http://www.bilibili.com/bangumi/i/" + bid;
				String content = asString( url );
				Bangumi b = new Bangumi( bid, content );
				return b;
			}
		} );
	}

	public List<Bangumi> getBangumiList() {
		return safeRun( new SafeRunner<List<Bangumi>>() {
			public List<Bangumi> run() throws Exception {
				String url = "http://www.bilibili.com/index/index-bangumi-timeline.json";
				String content = asString( url );
				//Bangumi b = new Bangumi( bid, content );
				JSONObject jo = JSON.parseObject( content );
				JSONArray ja = jo.getJSONObject( "bangumi" ).getJSONArray( "list" );
				List<Bangumi> list = new ArrayList<Bangumi>();
				for (int i = 0; i < ja.size(); ++i) {
					JSONObject jo2 = ja.getJSONObject( i );
					String bid = Integer.toString( jo2.getInteger( "season_id" ) );
					Bangumi b = getBangumi( bid );
					list.add( b );
				}
				return list;
			}
		} );
	}

	public FavGetList getFavoriteListJSON(final int pagesize) {
		return safeRun( new SafeRunner<FavGetList>() {
			public FavGetList run() throws Exception {
				HttpUriRequest req = makeGetFavoriteListRequest( pagesize );
				String content = asString( req );
				JSONObject data = JSON.parseObject( content ).getJSONObject( "data" );
				FavGetList json = JSON.toJavaObject( data, FavGetList.class );
				json.pagesize = pagesize;
				return json;
			}
		} );
	}

	/**
	 * 通过http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=方式获取
	 * 只能获取正常(state=0)的视频信息 其他统一为state=-1未知
	 * @param aid
	 * @return
	 */
	public Video getVideo0(final int aid) {
		return safeRun( new SafeRunner<Video>() {
			public Video run() throws Exception {
				String url = "http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=" + aid;
				JSONObject json = asJSON( url );
				Video v = new Video();
				v.aid = aid;
				v.description = json.getString( "description" );
				v.keywords = "";//这里没有keywords
				v.typeid = json.getIntValue( "tid" );//这里叫做tid
				v.title = json.getString( "title" );
				v.mid = json.getIntValue( "mid" );
				v.pic = json.getString( "pic" );
				v.status = 0;
				return v;
			}
		} );
	}

	public Video getVideo1(final int aid) {
		return safeRun( new SafeRunner<Video>() {
			public Video run() throws Exception {
				String url = "http://www.bilibili.com/video/av" + aid;
				String content = asString( url );
				Video v = new Video();
				v.aid = aid;
				Document d = Jsoup.parse( content );
				v.title = d.select( ".v-title" ).text();
				if (!v.title.isEmpty()) {
					v.state = 0;
				} else if (content.contains( "你没有权限浏览" ))
					v.state = 1;
				else if (content.contains( "此视频不存在或被删除." ))
					v.state = 2;
				else if (content.contains( "你输入的参数有误" ))
					v.state = 3;
				else if (content.contains( "本视频已撞车或被版权所有者申述" ))
					v.state = 4;
				else {
					System.out.println( content );
					throw new RuntimeException( "未知的状态! aid=" + aid );
				}
				return v;
			}
		} );
	}

	@Deprecated
	public Video getVideo2(int aid) {
		//这个方法有可能会出错
		//先收藏它
		addFavotite( aid );
		//理论上是期待list中有唯一的Video元素
		//但是可能由于各种原因 里面没有!

		//可以考虑在这里加一个sleep 但是代价太大了
		FavGetList list = getFavoriteListJSON( 50 );
		for (Video v : list.vlist) {
			if (aid == v.aid) {
				//删除它
				deleteFavorite( a.fid, aid );
				return v;
			}
		}
		//没有找到 可能需要重试一次
		return null;
	}

	/**
	 * 获取评论
	 * @return
	 */
	private HttpUriRequest makeCommentListRequest(int aid, int page, int pagesize) {
		return RequestBuilder.get( "http://api.bilibili.com/feedback" ).addParameter( "page", Integer.toString( page ) )
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
		JSONObject json = asJSON( req );
		return json.getIntValue( "results" ) == 0;
	}

	public boolean isLogin() {
		return safeRun( new SafeRunner<Boolean>() {
			public Boolean run() throws Exception {
				String url = "http://member.bilibili.com/main.html";
				String content = asString( url );
				return content.contains( Integer.toString( a.id ) );
			}
		} );
	}

	private JSONObject asJSON(HttpUriRequest req) {
		return JSON.parseObject( asString( req ) );
	}

	private JSONObject asJSON(String url) {
		return JSON.parseObject( asString( url ) );
	}

	private String asString(final HttpUriRequest req) {
		return safeRun( new SafeRunner<String>() {
			public String run() throws Exception {
				CloseableHttpResponse res = hc.execute( req, ctx );
				try {
					return EntityUtils.toString( res.getEntity(), UTF8 );
				} finally {
					HttpClientUtils.closeQuietly( res );
				}
			}
		} );
	}

	private String asString(final String url) {
		return asString( RequestBuilder.get( url ).build() );
	}

	private boolean initAccount() {
		if (!isLogin())
			return false;
		String url = FAV_GET_BOX_LIST_URL + a.id;
		JSONObject jo = asJSON( url );
		a.fid = jo.getJSONObject( "data" ).getJSONArray( "list" ).getJSONObject( 0 ).getIntValue( "fav_box" );
		return true;
	}

	/**
	 * 进行评论
	 * @param aid
	 * @param msg
	 * @return
	 */
	private HttpUriRequest makeCommentRequest(int aid, String msg) {
		return RequestBuilder.get( "http://interface.bilibili.com/feedback/post" ).addParameter( "callback", "abc" )
				.addParameter( "aid", Integer.toString( aid ) ).addParameter( "msg", msg )
				.addParameter( "action", "send" ).addHeader( "Referer", "http://www.bilibili.com/video/av" + aid )
				.build();
	}

	/**
	 * 从默认收藏夹中删除这些aid
	 * @param aids
	 * @return
	 */
	private HttpUriRequest makeDeleteFavoriteRequest(String aids) {
		return RequestBuilder.post( "http://space.bilibili.com/ajax/fav/mdel" )
				.addParameter( "fid", Integer.toString( a.fid ) ).addParameter( "aids", aids )
				.addHeader( "Referer", "http://space.bilibili.com/" ).build();
	}

	/**
	 * 获得默认收藏夹的内容
	 * @param pagesize
	 * @return
	 */
	private HttpUriRequest makeGetFavoriteListRequest(int pagesize) {
		return RequestBuilder.get( "http://space.bilibili.com/ajax/fav/getList" )
				.addParameter( "mid", Integer.toString( a.id ) ).addParameter( "fid", Integer.toString( a.fid ) )
				.addParameter( "pagesize", Integer.toString( pagesize ) ).addParameter( "order", "ftime" ).build();
	}

	private <T> T safeRun(SafeRunner<T> sr) {
		try {
			return sr.run();
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException( e );
		}
	}

}
