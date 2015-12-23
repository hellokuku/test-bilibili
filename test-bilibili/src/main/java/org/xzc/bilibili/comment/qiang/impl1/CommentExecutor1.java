package org.xzc.bilibili.comment.qiang.impl1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.util.Sign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CommentExecutor1 extends CommentExecutor {
	private static final Logger log = Logger.getLogger( CommentExecutor1.class );

	public CommentExecutor1(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		super( cfg, stop, last );
	}

	protected HttpUriRequest makeCommentRequest() {
		Map<String, String> params = new HashMap<String, String>();
		params.put( "access_key", cfg.getAccessKey() );
		params.put( "appkey", Sign.appkey );
		params.put( "platform", "android" );
		params.put( "_device", "android" );
		params.put( "type", "json" );
		params.put( "aid", Integer.toString( cfg.getAid() ) );
		Sign s = new Sign( params );
		params.put( "sign", s.getSign() );
		UrlEncodedFormEntity entity = null;
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add( new BasicNameValuePair( "msg", cfg.getMsg() ) );
		list.add( new BasicNameValuePair( "mid", cfg.getMid() ) );
		try {
			entity = new UrlEncodedFormEntity( list, "utf-8" );
		} catch (Exception ex) {
		}
		RequestBuilder rb = RequestBuilder.post( "http://" + cfg.getServerIP() + "/feedback/post" ).setEntity( entity );
		rb.addHeader( "Host", "api.bilibili.com" );
		rb.addHeader( "User-Agent", "Mozilla/5.0 BiliDroid/3.3.0 (bbcallen@gmail.com)" );
		for (Entry<String, String> e : params.entrySet()) {
			rb.addParameter( e.getKey(), e.getValue() );
		}
		return rb.build();
	}

	@Override
	protected WorkResult workInternal(String content, HttpUriRequest req) {
		JSONObject json = JSON.parseObject( content );
		int code = json.getIntValue( "code" );
		switch (code) {
		case 0:
		case -105://成功 -105是验证码问题
			return WorkResult.STOP;
		case -404://还不可评论
			return WorkResult.NORMAL;
		case -503:
		case -103://超速 //{"code":-103,"message":"Credits is not enought.","ts":1450673739}
			return WorkResult.OVERSPEED;
		default:
			if (log.isDebugEnabled())
				log.debug( "出现了意外的code=" + code + " content=" + content );
			return WorkResult.STOP;
		}
	}

}
