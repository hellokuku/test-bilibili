package org.xzc.bilibili.comment.qiang;

import java.util.Date;

public class Config {
	private int aid;
	private String msg;
	private String tag;
	private int batch;
	private Date endAt;
	private Date startAt;
	private String sip;
	private String DedeUserID;
	private String mid;
	private String accessKey;

	public String getMid() {
		return mid;
	}

	public Config setMid(String mid) {
		this.mid = mid;
		return this;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public Config setAccessKey(String accessKey) {
		this.accessKey = accessKey;
		return this;
	}

	public int getMode() {
		return mode;
	}

	public Config setMode(int mode) {
		this.mode = mode;
		return this;
	}

	private String SESSDATA;
	private int interval;
	private boolean stopWhenForbidden;
	private int mode;

	public boolean isStopWhenForbidden() {
		return stopWhenForbidden;
	}

	public Config setStopWhenForbidden(boolean stopWhenForbidden) {
		this.stopWhenForbidden = stopWhenForbidden;
		return this;
	}

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Config() {
		// this(null, 0, 0, null, null, 0);
	}

	public Config(
			int mode, String sip,
			String DedeUserID, String SESSDATA,
			String mid, String accessKey,
			int batch, int interval, boolean stopWhenForbidden,
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

		this.tag = tag;
		this.aid = aid;
		this.msg = msg;
		this.startAt = startAt;
		this.endAt = endAt;
	}

	public Config custom(String tag, int aid, String msg, Date startAt, Date endAt) {
		return new Config(
				mode, sip,
				DedeUserID, SESSDATA,
				mid, accessKey,
				batch, interval, stopWhenForbidden,
				tag, aid, msg, startAt, endAt );
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

	public Config setTag(String tag) {
		this.tag = tag;
		return this;
	}

	public String getSip() {
		return sip;
	}

	public Config setSip(String sip) {
		this.sip = sip;
		return this;
	}

	public String getDedeUserID() {
		return DedeUserID;
	}

	public Config setDedeUserID(String dedeUserID) {
		DedeUserID = dedeUserID;
		return this;
	}

	public String getSESSDATA() {
		return SESSDATA;
	}

	public Config setSESSDATA(String sESSDATA) {
		SESSDATA = sESSDATA;
		return this;
	}

	public int getInterval() {
		return interval;
	}

	public Config setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	@Override
	public String toString() {
		return "Config [aid=" + aid + ", msg=" + msg + ", tag=" + tag + ", batch=" + batch + ", endAt=" + endAt
				+ ", startAt=" + startAt + "]";
	}

}
