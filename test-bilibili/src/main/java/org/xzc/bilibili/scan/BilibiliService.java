package org.xzc.bilibili.scan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.Bangumi;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Result;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.util.HC;
import org.xzc.bilibili.util.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class BilibiliService {
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
	 * 绑定的hc
	 */
	private HC hc;

	/**
	 * 绑定的账号
	 */
	private Account a;

	public BilibiliService(final Account account) {
		this.a = account;
		//控制连接并发量
		PoolingHttpClientConnectionManager m = new PoolingHttpClientConnectionManager();
		m.setDefaultMaxPerRoute( 4 );
		m.setMaxTotal( 20 );

		//忽略cookie
		RequestConfig rc = RequestConfig.custom().setCookieSpec( CookieSpecs.IGNORE_COOKIES ).build();
		CloseableHttpClient chc = HttpClients.custom().setDefaultRequestConfig( rc )
				.addInterceptorFirst( new HttpRequestInterceptor() {
					public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
						request.addHeader( "Cookie",
								"DedeUserID=" + a.getId() + "; SESSDATA=" + a.getSESSIDATA() + ";" );
					}
				} ).setConnectionManager( m ).build();

		hc = new HC( chc );

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
		String url = "http://api.bilibili.com/favourite/add?id=" + aid;
		String content = hc.getAsString( url );
		return JSON.parseObject( content ).getIntValue( "code" );
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
		String res = hc.asString( req );
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
		String content = hc.asString( req );
		return content;
	}

	public String deleteFavorite2(String aids) {
		HttpUriRequest req = RequestBuilder.post( "http://space.bilibili.com/ajax/fav/mdel" )
				.addParameter( "fid", Integer.toString( a.getFid() ) ).addParameter( "aids", aids )
				.addHeader( "Referer", "http://space.bilibili.com/" ).build();
		String content = hc.asString( req );
		return content;
	}

	public Result deleteFavoriteJSON(FavGetList fgl) {
		if (fgl.vlist.size() == 0) {
			return new Result( true, "要删除的视频id为空, 不发请求." );
		}
		String aids = getDeleteAids( fgl );
		HttpUriRequest req = makeDeleteFavoriteRequest( aids );
		String result = hc.asString( req );
		JSONObject json = JSON.parseObject( result );
		return new Result( json.getBooleanValue( "status" ), json.getString( "data" ) );
	}

	public Bangumi getBangumi(final String bid) {
		String url = "http://www.bilibili.com/bangumi/i/" + bid;
		String content = hc.getAsString( url );
		Bangumi b = new Bangumi( bid, content );
		return b;
	}

	public List<Bangumi> getBangumiList() {
		String url = "http://www.bilibili.com/index/index-bangumi-timeline.json";
		String content = hc.getAsString( url );
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
	 * 通过http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=方式获取
	 * 只能获取正常(state=0)的视频信息 其他统一为state=-1未知
	 * @param aid
	 * @return
	 */
	public Video getVideo0(final int aid) {
		String url = "http://api.bilibili.cn/view?appkey=03fc8eb101b091fb&id=" + aid;
		JSONObject json = hc.getAsJSON( url );
		Video v = new Video();
		v.aid = aid;
		v.typeid = json.getIntValue( "tid" );//这里叫做tid
		v.title = json.getString( "title" );
		v.mid = json.getIntValue( "mid" );
		v.status = 0;
		return v;
	}

	@Deprecated
	public Video getVideo1(final int aid) {
		String url = "http://www.bilibili.com/video/av" + aid;
		String content = hc.getAsString( url );
		Video v = new Video();
		v.aid = aid;
		Document d = Jsoup.parse( content );
		v.title = d.select( ".v-title" ).text();
		if (!v.title.isEmpty())
			v.status = 0;
		else
			v.status = -1;
		return v;
	}

	@Deprecated
	public Video getVideo2(int aid) {
		if (true)
			throw new UnsupportedOperationException( "不要使用这个方法" );
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
				deleteFavorite( a.getFid(), aid );
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
		JSONObject json = hc.asJSON( req );
		return json.getIntValue( "results" ) == 0;
	}

	public boolean isLogin() {
		String url = "http://member.bilibili.com/main.html";
		String content = hc.getAsString( url );
		return content.contains( Integer.toString( a.getId() ) );
	}

	private boolean initAccount() {
		String url = "http://member.bilibili.com/main.html";
		String content = hc.getAsString( url );
		if (!content.contains( Integer.toString( a.getId() ) ))//账号还没有登陆
			return false;
		String jsonStr = hc.getAsString( "http://api.bilibili.com/userinfo?mid=" + a.getId() );

		Account aa = JSON.parseObject( jsonStr, Account.class );
		aa.setSESSIDATA( a.getSESSIDATA() );
		aa.setId( a.getId() );
		a = aa;

		a.setActive( !content.contains( "我要回答问题激活" ) );//要不要顺便去激活一下?

		//获得默认的收藏夹
		JSONObject jo = hc.getAsJSON( FAV_GET_BOX_LIST_URL + a.getId() );
		a.setFid( jo.getJSONObject( "data" ).getJSONArray( "list" ).getJSONObject( 0 ).getIntValue( "fav_box" ) );

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
		return RequestBuilder.get( "http://interface.bilibili.com/feedback/post" )
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
		return RequestBuilder.post( "http://space.bilibili.com/ajax/fav/mdel" )
				.addHeader( "Origin", "http://space.bilibili.com" )
				.addHeader( "X-Requested-With", "XMLHttpRequest" )
				.addHeader( "User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.87 Safari/537.36 QQBrowser/9.2.5584.400" )
				//.setEntity( HC.makeFormEntity( "fid", a.getFid(), "aids", aids ) )
				.addParameter( "fid", Integer.toString( a.getFid() ) ).addParameter( "aids", aids )
				.addHeader( "Referer", "http://space.bilibili.com/" ).build();
	}

	/**
	 * 获得默认收藏夹的内容
	 * @param pagesize
	 * @return
	 */
	private HttpUriRequest makeGetFavoriteListRequest(int pagesize) {
		return RequestBuilder.get( "http://space.bilibili.com/ajax/fav/getList" )
				.addParameter( "mid", Integer.toString( a.getId() ) )
				.addParameter( "fid", Integer.toString( a.getFid() ) )
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
}
