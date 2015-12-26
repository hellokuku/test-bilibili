package org.xzc.bilibili.comment.qiang.impl1;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.http.Params;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CommentExecutor3 extends CommentExecutor {
	public CommentExecutor3(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		super( cfg, stop, last );
	}

	@Override
	protected HttpUriRequest makeCommentRequest() {
		return RequestBuilder.post( "http://" + cfg.getServerIP() + "/x/reply/add" )
				.addHeader( "Host", "api.bilibili.com" )
				.addHeader( "Origin", "http://www.bilibili.com" )
				.addHeader( "Cookie", "DedeUserID=" + cfg.getDedeUserID() + "; SESSDATA=" + cfg.getSESSDATA() + ";" )
				.addHeader( "Referer", "http://www.bilibili.com/video/av" + cfg.getAid() + "/" )
				.setEntity(
						new Params( "jsonp", "json", "message", cfg.getMsg(), "oid", cfg.getAid(), "type", 1 )
								.toEntity() )
				.build();
	}

	@Override
	protected WorkResult workInternal(String content, HttpUriRequest req) {
		try {
			JSONObject json = JSON.parseObject( content );
			int code = json.getIntValue( "code" );
			if (code == 0)
				return WorkResult.STOP;
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return WorkResult.OVERSPEED;
	}

}
