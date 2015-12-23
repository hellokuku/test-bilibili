package org.xzc.bilibili.comment.qiang;

import org.apache.http.HttpHost;

public class Proxy {
	public String ip;
	public int port;

	public Proxy() {
	}

	public Proxy(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	public String toString() {
		return ip + ":" + port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( ip == null ) ? 0 : ip.hashCode() );
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Proxy other = (Proxy) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals( other.ip ))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}