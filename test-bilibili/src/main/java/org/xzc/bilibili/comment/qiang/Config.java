package org.xzc.bilibili.comment.qiang;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Config {

	private int batch; //线程数

	private String tag; //一个tag用于表示本次评论任务
	private int aid; //视频的aid
	private String msg; //评论的消息
	private Date endAt; //超过这个时间就不再评论
	private Date startAt; //开始评论的时间
	private String sip; //服务器的ip地址

	private String DedeUserID; //用于Cookie
	private String SESSDATA; //用于Cookie

	private String mid; //用于api的参数
	private String accessKey; //用于api的参数

	private boolean diu = true; //是否统计丢失问题
	private int interval; //间隔多少个评论就打印一次信息
	private boolean stopWhenForbidden = true; //当禁言的时候停止评论

	private int mode; //评论的方法 基于cookie还是基于api调用

	private List<String> senderList = new ArrayList<String>();

	public Config() {
	}

	public List<String> getSenderList() {
		return senderList;
	}

	public Config setSenderList(List<String> senderList) {
		this.senderList = senderList;
		return this;
	}

	public Config(
			int mode, String sip,
			String DedeUserID, String SESSDATA,
			String mid, String accessKey,
			int batch, int interval, boolean stopWhenForbidden, boolean diu,
			String tag, int aid, String msg, Date startAt, Date endAt) {

		this.mode = mode;
		this.sip = sip;

		this.DedeUserID = DedeUserID;
		this.SESSDATA = SESSDATA;

		this.mid = mid;
		this.accessKey = accessKey;

		this.batch = batch;
		this.interval = interval;
		this.stopWhenForbidden = stopWhenForbidden;
		this.diu = diu;

		this.tag = tag;
		this.aid = aid;
		this.msg = msg;
		this.startAt = startAt;
		this.endAt = endAt;
	}

	public Config(
			int mode, String sip,
			String DedeUserID, String SESSDATA,
			String mid, String accessKey,
			int batch, int interval, boolean stopWhenForbidden, boolean diu,
			String tag, int aid, String msg, Date startAt, Date endAt, List<String> senderList) {
		this( mode, sip, DedeUserID, SESSDATA, mid, accessKey, batch, interval, stopWhenForbidden, diu, tag, aid, msg,
				startAt, endAt );
		this.senderList = senderList;
	}

	public Config custom(String tag, int aid, String msg, Date startAt, Date endAt) {
		return new Config(
				mode, sip,
				DedeUserID, SESSDATA,
				mid, accessKey,
				batch, interval, stopWhenForbidden, diu,
				tag, aid, msg, startAt, endAt, senderList );
	}

	public Config custom() {
		return new Config(
				mode, sip,
				DedeUserID, SESSDATA,
				mid, accessKey,
				batch, interval, stopWhenForbidden, diu,
				tag, aid, msg, startAt, endAt, senderList );
	}

	public String getAccessKey() {
		return accessKey;
	}

	public int getAid() {
		return aid;
	}

	public int getBatch() {
		return batch;
	}

	public String getDedeUserID() {
		return DedeUserID;
	}

	public Date getEndAt() {
		return endAt;
	}

	public int getInterval() {
		return interval;
	}

	public String getMid() {
		return mid;
	}

	public int getMode() {
		return mode;
	}

	public String getMsg() {
		return msg;
	}

	public String getSESSDATA() {
		return SESSDATA;
	}

	public String getSip() {
		return sip;
	}

	public Date getStartAt() {
		return startAt;
	}

	public String getTag() {
		return tag;
	}

	public boolean isDiu() {
		return diu;
	}

	public boolean isStopWhenForbidden() {
		return stopWhenForbidden;
	}

	public Config setAccessKey(String accessKey) {
		this.accessKey = accessKey;
		return this;
	}

	public Config setAid(int aid) {
		this.aid = aid;
		return this;
	}

	public Config setBatch(int batch) {
		this.batch = batch;
		return this;
	}

	public Config setDedeUserID(String dedeUserID) {
		DedeUserID = dedeUserID;
		return this;
	}

	public Config setDiu(boolean diu) {
		this.diu = diu;
		return this;
	}

	public Config setEndAt(Date endAt) {
		this.endAt = endAt;
		return this;
	}

	public Config setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	public Config setMid(String mid) {
		this.mid = mid;
		return this;
	}

	public Config setMode(int mode) {
		this.mode = mode;
		return this;
	}

	public Config setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public Config setSESSDATA(String sESSDATA) {
		SESSDATA = sESSDATA;
		return this;
	}

	public Config setSip(String sip) {
		this.sip = sip;
		return this;
	}

	public Config setStartAt(Date startAt) {
		this.startAt = startAt;
		return this;
	}

	public Config setStopWhenForbidden(boolean stopWhenForbidden) {
		this.stopWhenForbidden = stopWhenForbidden;
		return this;
	}

	public Config setTag(String tag) {
		this.tag = tag;
		return this;
	}

	@Override
	public String toString() {
		return "Config [aid=" + aid + ", msg=" + msg + ", tag=" + tag + ", batch=" + batch + ", endAt=" + endAt
				+ ", startAt=" + startAt + "]";
	}

}
