package org.xzc.bilibili.comment.qiang.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.xzc.bilibili.comment.qiang.Proxy;

public class CommentJobConfig implements Cloneable {
	String tag;
	Date startAt;
	CommentConfig commentConfig;
	List<Proxy> proxyList;
	int mode = -1;

	public CommentJobConfig() {
	}

	public CommentJobConfig clone() {
		try {
			return (CommentJobConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException( e );
		}
	}

	public CommentJobConfig addProxy(Proxy proxy) {
		if (proxyList == null)
			proxyList = new ArrayList<Proxy>();
		proxyList.add( proxy );
		return this;
	}

	public String getTag() {
		return tag;
	}

	public CommentJobConfig setTag(String tag) {
		this.tag = tag;
		return this;
	}

	public Date getStartAt() {
		return startAt;
	}

	public CommentJobConfig setStartAt(Date startAt) {
		this.startAt = startAt;
		return this;
	}

	public CommentConfig getCommentConfig() {
		return commentConfig;
	}

	public CommentJobConfig setCommentConfig(CommentConfig commentConfig) {
		this.commentConfig = commentConfig;
		return this;
	}

	public List<Proxy> getProxyList() {
		return proxyList;
	}

	public CommentJobConfig setProxyList(List<Proxy> proxyList) {
		this.proxyList = proxyList;
		return this;
	}

	public int getMode() {
		return mode;
	}

	public CommentJobConfig setMode(int mode) {
		this.mode = mode;
		return this;
	}

	public CommentJobConfig addProxy(String proxyHost, int proxyPort) {
		return addProxy( new Proxy( proxyHost, proxyPort ) );
	}
}
