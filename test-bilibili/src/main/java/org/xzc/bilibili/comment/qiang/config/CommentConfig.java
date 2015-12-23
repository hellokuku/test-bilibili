package org.xzc.bilibili.comment.qiang.config;

import java.util.Date;

import org.joda.time.DateTime;
import org.xzc.bilibili.comment.qiang.Proxy;

public class CommentConfig implements Cloneable {

	String serverIP; //服务器的ip地址

	int batch; //线程数
	int interval; //间隔多少个评论就打印一次信息

	boolean diu; //是否统计丢失问题
	boolean stopWhenForbidden; //当禁言的时候停止评论
	int timeout;

	Proxy proxy;

	String tag; //一个tag用于表示本次评论任务
	int aid; //视频的aid
	String msg; //评论的消息
	Date endAt; //超过这个时间就不再评论

	String DedeUserID; //用于Cookie
	String SESSDATA; //用于Cookie

	String mid; //用于api的参数
	String accessKey; //用于api的参数

	public CommentConfig() {
	}

	public int getBatch() {
		return batch;
	}

	public String getTag() {
		return tag;
	}

	public int getAid() {
		return aid;
	}

	public String getMsg() {
		return msg;
	}

	public Date getEndAt() {
		return endAt;
	}

	public String getServerIP() {
		return serverIP;
	}

	public String getDedeUserID() {
		return DedeUserID;
	}

	public String getSESSDATA() {
		return SESSDATA;
	}

	public String getMid() {
		return mid;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public boolean isDiu() {
		return diu;
	}

	public CommentConfig setServerIP(String serverIP) {
		this.serverIP = serverIP;
		return this;
	}

	public CommentConfig setBatch(int batch) {
		this.batch = batch;
		return this;
	}

	public CommentConfig setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	public CommentConfig setDiu(boolean diu) {
		this.diu = diu;
		return this;
	}

	public CommentConfig setStopWhenForbidden(boolean stopWhenForbidden) {
		this.stopWhenForbidden = stopWhenForbidden;
		return this;
	}

	public CommentConfig setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public CommentConfig setTag(String tag) {
		this.tag = tag;
		return this;
	}

	public CommentConfig setAid(int aid) {
		this.aid = aid;
		return this;
	}

	public CommentConfig setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public CommentConfig setEndAt(Date endAt) {
		this.endAt = endAt;
		return this;
	}

	public CommentConfig setDedeUserID(String dedeUserID) {
		DedeUserID = dedeUserID;
		return this;
	}

	public CommentConfig setSESSDATA(String sESSDATA) {
		SESSDATA = sESSDATA;
		return this;
	}

	public CommentConfig setMid(String mid) {
		this.mid = mid;
		return this;
	}

	public CommentConfig setAccessKey(String accessKey) {
		this.accessKey = accessKey;
		return this;
	}

	public int getInterval() {
		return interval;
	}

	public boolean isStopWhenForbidden() {
		return stopWhenForbidden;
	}

	public int getTimeout() {
		return timeout;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public CommentConfig setProxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	public CommentConfig clone() {
		try {
			return (CommentConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException( e );
		}
	}

	public CommentConfig thread(int batch, int interval) {
		this.batch = batch;
		this.interval = interval;
		return this;
	}

	public CommentConfig other(boolean diu, boolean stopWhenForbidden) {
		this.diu = diu;
		this.stopWhenForbidden = stopWhenForbidden;
		return this;
	}

	public CommentConfig video(int aid, String msg, DateTime endAt) {
		this.aid = aid;
		this.msg = msg;
		this.endAt = endAt.toDate();
		return this;
	}

	public CommentConfig cookie(String DedeUserID, String SESSDATA) {
		this.DedeUserID = DedeUserID;
		this.SESSDATA = SESSDATA;
		return this;
	}

	public CommentConfig api2(String mid, String accessKey) {
		this.mid = mid;
		this.accessKey = accessKey;
		return this;
	}

}
