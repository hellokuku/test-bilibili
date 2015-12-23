package org.xzc.bilibili.comment.qiang.select;

import org.xzc.bilibili.comment.qiang.config.CommentConfig;

public class CommentResult {
	private CommentConfig commentConfig;
	private int count;
	private int diu;

	public CommentConfig getCommentConfig() {
		return commentConfig;
	}

	public void setCommentConfig(CommentConfig commentConfig) {
		this.commentConfig = commentConfig;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getDiu() {
		return diu;
	}

	public void setDiu(int diu) {
		this.diu = diu;
	}

}
