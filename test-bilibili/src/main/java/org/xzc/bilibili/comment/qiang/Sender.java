package org.xzc.bilibili.comment.qiang;

public class Sender {
	private String ip;
	private int port;
	private int batch;
	private String tag;
	
	public Sender() {
	}

	public Sender(int batch, String tag) {
		this.ip = ip;
		this.port = port;
		this.batch = batch;
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getBatch() {
		return batch;
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
