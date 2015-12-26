package org.xzc.bilibili.comment.qiang.impl1;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.comment.qiang.config.CommentJobConfig;

public class CommentExecutorFactory {
	public static CommentExecutor createCommentExecutor(CommentJobConfig jobCfg, CommentConfig cfg, AtomicBoolean stop,
			AtomicLong last) {
		switch (jobCfg.getMode()) {
		case -1:
			return new MockCommentExecutor( cfg, stop, last );
		case 0:
			return new CommentExecutor0( cfg, stop, last );
		case 1:
			return new CommentExecutor1( cfg, stop, last );
		case 2:
			return new CommentExecutor2( cfg, stop, last );
		case 3:
			return new CommentExecutor3( cfg, stop, last );
		default:
			throw new IllegalArgumentException( "不支持的mode=" + jobCfg.getMode() );
		}
	}
}
