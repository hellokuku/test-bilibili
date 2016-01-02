package org.xzc.bilibili.comment.qiang.impl1;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.xzc.bilibili.api2.ApiUtils;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.comment.qiang.impl1.CommentExecutor.WorkResult;
import org.xzc.http.HC;
import org.xzc.http.Req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CommentExecutor4 extends CommentExecutor {

	public CommentExecutor4(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		super( cfg, stop, last );
	}

	@Override
	protected HttpUriRequest makeCommentRequest() {
		Req videoReq = ApiUtils.api().get( "/x/video?aid=" + cfg.getAid() );
		return videoReq.build();
	}

	protected void work2(CloseableHttpClient chc, ExecutorService es) throws InterruptedException, ExecutionException {
		String cookie = "DedeUserID=" + cfg.getDedeUserID() + "; SESSDATA=" + cfg.getSESSDATA() + ";";
		Req videoReq = ApiUtils.api().get( "/x/video?aid=" + cfg.getAid() );
		//Req replyReq = ApiUtils.api().get( "/x/reply?type=1&oid=" + cfg.getAid() );
		Req addReplyReq = ApiUtils.api().post( "/x/reply/add" )
				.header( "Cookie", cookie )
				.datas( "type", 1, "oid", cfg.getAid(), "message",
						cfg.getMsg() );
		HC hc = new HC( chc );
		int exceptionCount = 0;
		while (!stop.get()) {
			try {
				JSONObject videoJSON = hc.asJSON( videoReq );
				if (videoJSON.getJSONObject( "data" ).getIntValue( "status" ) == 0) {
					String content = hc.asString( addReplyReq );
					System.out.println( content );
					stop.set( true );
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (++exceptionCount >= 100)
					break;
			}
		}
	}

	@Override
	protected WorkResult workInternal(String content, HttpUriRequest req, CloseableHttpClient chc) {
		JSONObject videoJSON = JSON.parseObject( content );
		if (videoJSON.getJSONObject( "data" ).getIntValue( "status" ) == 0) {
			HC hc = new HC( chc );
			String cookie = "DedeUserID=" + cfg.getDedeUserID() + "; SESSDATA=" + cfg.getSESSDATA() + ";";
			Req addReplyReq = ApiUtils.api().post( "/x/reply/add" )
					.header( "Cookie", cookie )
					.datas( "type", 1, "oid", cfg.getAid(), "message",
							cfg.getMsg() );
			try {
				content = hc.asString( addReplyReq );
				System.out.println( content );
			} catch (Exception e) {
				e.printStackTrace();
			}
			return WorkResult.STOP;
		} else {
			return WorkResult.DIU;
		}
	}

	@Override
	protected WorkResult workInternal(String content, HttpUriRequest req) {
		return null;
	}

}
