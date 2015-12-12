package org.xzc.bilibili.comment;

import org.apache.http.HttpHost;

public class Config {
	public int batch;
	public int aid;
	public String msg;
	public HttpHost proxy;
	public long delay;
	public String tag;
	public String sip;
	public String fip;

	public Config() {
		// this(null, 0, 0, null, null, 0);
	}

	public Config(String tag, int batch, int aid, String msg, HttpHost proxy, long delay, String sip, String fip) {
		this.tag = tag;
		this.batch = batch;
		this.aid = aid;
		this.msg = msg;
		this.proxy = proxy;
		this.delay = delay;
		this.sip = sip;
	}

	public Config copy() {
		return new Config( tag, batch, aid, msg, proxy, delay, sip,fip );
	}

	public String getFip() {
		return fip;
	}

	public Config setFip(String fip) {
		this.fip = fip;
		return this;
	}

	public String getSip() {
		return sip;
	}

	public Config setSip(String sip) {
		this.sip = sip;
		return this;
	}

	public int getBatch() {
		return batch;
	}

	public Config setBatch(int batch) {
		this.batch = batch;
		return this;
	}

	public int getAid() {
		return aid;
	}

	public Config setAid(int aid) {
		this.aid = aid;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public Config setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public HttpHost getProxy() {
		return proxy;
	}

	public Config setProxy(HttpHost proxy) {
		this.proxy = proxy;
		return this;
	}

	public long getDelay() {
		return delay;
	}

	public Config setDelay(long delay) {
		this.delay = delay;
		return this;
	}

	public String getTag() {
		return tag;
	}

	public Config setTag(String tag) {
		this.tag = tag;
		return this;
	}

}
