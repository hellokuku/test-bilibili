package org.xzc.bilibili.model;

import org.apache.http.HttpHost;

public  class Config {
	public int batch;
	public int aid;
	public String msg;
	public HttpHost proxy;
	public long delay;
	public String tag;
	public Config(String tag, int batch, int aid, String msg, HttpHost proxy, long delay) {
		super();
		this.tag = tag;
		this.batch = batch;
		this.aid = aid;
		this.msg = msg;
		this.proxy = proxy;
		this.delay = delay;
	}
}
