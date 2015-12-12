package org.xzc.bilibili.proxy;

public class ProxyPair {
	private String ip;
	private int port;

	public ProxyPair(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}

	public ProxyPair() {
		super();
	}

	@Override
	public String toString() {
		return "ProxyPair [ip=" + ip + ", port=" + port + "]";
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
