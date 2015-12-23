package org.xzc.bilibili.comment.qiang.impl1;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;

public class MockCommentExecutor extends CommentExecutor {
	public MockCommentExecutor(CommentConfig cfg, AtomicBoolean stop, AtomicLong last) {
		super( cfg, stop, last );
	}

	@Override
	protected HttpUriRequest makeCommentRequest() {
		return null;
	}

	@Override
	protected WorkResult workInternal(String content, HttpUriRequest req) {
		if ("OK".equals( content ) || content.contains( "验证码" ) || content.contains( "禁言" )) {
			return WorkResult.STOP;
		}
		return WorkResult.NORMAL;
	}

	private Random random = new Random();

	private AtomicBoolean commented = new AtomicBoolean( false );
	private AtomicInteger count = new AtomicInteger( 0 );

	@Override
	protected String execute(CloseableHttpClient chc, HttpUriRequest req) throws ClientProtocolException, IOException {
		try {
			Thread.sleep( random.nextInt( 100 ) + 200 );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (commented.get()) {
			if (count.incrementAndGet() > 5) {
				return "验证码错误";
			} else {
				return "OK";
			}
		} else {
			if (random.nextInt( 65536 ) == 0) {
				commented.set( true );
				if (count.incrementAndGet() > 5) {
					return "验证码错误";
				} else {
					return "OK";
				}
			} else {
				return "无法对该文档进行评论!";
			}
		}
	}

}
