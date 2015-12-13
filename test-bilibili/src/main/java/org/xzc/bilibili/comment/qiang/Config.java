package org.xzc.bilibili.comment.qiang;

import java.util.ArrayList;
import java.util.Date;
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
	private Date endAt;
	private Date startAt;

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Config() {
		// this(null, 0, 0, null, null, 0);
	}

	public Config(String tag, int aid, String msg, Date startAt, Date endAt) {
		this.tag = tag;
		this.aid = aid;
		this.msg = msg;
		this.startAt = startAt;
		this.endAt = endAt;
	}

	public Config copy() {
		return new Config( tag, aid, msg, startAt, endAt );
	}

	public int getAid() {
		return aid;
	}

	public int getBatch() {
		return batch;
	}

	public Date getEndAt() {
		return endAt;
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

	public Config setEndAt(Date endAt) {
		this.endAt = endAt;
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

	@Override
	public String toString() {
		return "Config [aid=" + aid + ", msg=" + msg + ", proxyHost=" + proxyHost + ", proxyPort=" + proxyPort
				+ ", tag=" + tag + ", subTag=" + subTag + ", senderList=" + senderList + ", batch=" + batch + "]";
	}

}
