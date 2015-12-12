package org.xzc.bilibili.comment;

import java.util.ArrayList;
import java.util.List;

public class Config {
	private int aid;
	private String msg;
	private String proxyHost;
	private int proxyPort;
	private String tag;
	private String subTag;
	private List<Sender> senderList = new ArrayList<Sender>();
	private int batch;

	public Config() {
		// this(null, 0, 0, null, null, 0);
	}

	public Config(String tag, int aid, String msg) {
		this.tag = tag;
		this.aid = aid;
		this.msg = msg;
	}

	public Config copy() {
		return new Config( tag, aid, msg );
	}

	public int getAid() {
		return aid;
	}

	public int getBatch() {
		return batch;
	}

	public String getMsg() {
		return msg;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public List<Sender> getSenderList() {
		return senderList;
	}

	public String getSubTag() {
		return subTag;
	}

	public String getTag() {
		return tag;
	}

	public Config setAid(int aid) {
		this.aid = aid;
		return this;
	}

	public Config setBatch(int batch) {
		this.batch = batch;
		return this;
	}

	public Config setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public Config setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
		return this;
	}

	public Config setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
		return this;
	}

	public Config setSenderList(List<Sender> senderList) {
		this.senderList = senderList;
		return this;
	}

	public Config setSubTag(String subTag) {
		this.subTag = subTag;
		return this;
	}

	public Config setTag(String tag) {
		this.tag = tag;
		return this;
	}

}
