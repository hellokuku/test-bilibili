package org.xzc.bilibili.comment.qiang.impl1;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.xzc.bilibili.api.Params;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.util.Utils;

public class CommentExecutor2 extends CommentExecutor {

	public CommentExecutor2(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		super( cfg, stop, last );
	}

	protected HttpUriRequest makeCommentRequest() {
		return RequestBuilder.post( "http://" + cfg.getServerIP() + "/feedback/post" )
				.addHeader( "User-Agent", "Mozilla/5.0 BiliDroid/2.3.4 (bbcallen@gmail.com)" )
				.addHeader( "Cookie", "DedeUserID=" + cfg.getDedeUserID() + "; SESSDATA=" + cfg.getSESSDATA() + ";" )
				.addHeader( "Referer", "http://www.bilibili.com" )
				.addHeader( "Host", "www.bilibili.com" )
				.setEntity(
						new Params( "aid", cfg.getAid(), "msg", cfg.getMsg(), "platform",
								"android"/*, "appkey", "03fc8eb101b091fb"*/ )
										.toEntity() )
				.build();
	}

	@Override
	protected WorkResult workInternal(String content, HttpUriRequest req) {
		if (cfg.isDiu() && content.length() > 100) {
			diu.incrementAndGet();
			return WorkResult.DIU;
		}
		if ("OK".equals( content )) {
			Utils.log( "成功了" + req.getURI().getHost() );
			return WorkResult.STOP;
		} else if ("OK".equals( content ) || content.contains( "验证码" )
				|| ( cfg.isStopWhenForbidden() && content.contains( "禁言" ) )) {
			return WorkResult.STOP;
		}
		return WorkResult.NORMAL;
	}

}
