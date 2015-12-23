package org.xzc.bilibili.comment.qiang.impl1;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.util.Utils;

public class CommentExecutor0 extends CommentExecutor {

	public CommentExecutor0(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		super( cfg, stop, last );
	}

	@Override
	protected String execute(CloseableHttpClient chc, HttpUriRequest req) throws ClientProtocolException, IOException {
		String content = super.execute( chc, req );
		if (content.length() > 100)
			return content;
		return Utils.decodeUnicode( content );
	}

	protected HttpUriRequest makeCommentRequest() {
		return RequestBuilder.get( "http://" + cfg.getServerIP() + "/feedback/post" )
				.addHeader( "Cookie", "DedeUserID=" + cfg.getDedeUserID() + "; SESSDATA=" + cfg.getSESSDATA() + ";" )
				.addHeader( "Host", "interface.bilibili.com" )
				.addParameter( "callback", "abc" )
				.addParameter( "aid", Integer.toString( cfg.getAid() ) ).addParameter( "msg", cfg.getMsg() )
				.addParameter( "action", "send" )
				.addHeader( "Referer", "http://www.bilibili.com/video/av" + cfg.getAid() )
				.build();
	}

	private static final Pattern RESULT_PATTERN = Pattern.compile( "abc\\(\"(.+)\"\\)" );

	@Override
	protected WorkResult workInternal(String content) {
		if (cfg.isDiu() && content.length() > 100) {
			diu.incrementAndGet();
			return WorkResult.NORMAL;
		}
		Matcher m = RESULT_PATTERN.matcher( content );
		if (m.find()) {
			String code = m.group( 1 );
			if ("OK".equals( code ) || code.contains( "验证码" )
					|| ( cfg.isStopWhenForbidden() && code.contains( "禁言" ) )) {
				return WorkResult.STOP;
			}
		}
		return WorkResult.NORMAL;
	}

}
