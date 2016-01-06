package org.xzc.bilibili.api2;

import org.xzc.http.Req;

public class ApiUtils {
	public static final String DEFAULT_IP = "218.205.74.9";//14.152.58.20 61.164.47.167
	public static final String API_HOST = "api.bilibili.com";
	public static final String API_IP = DEFAULT_IP;
	public static final String ACCOUNT_HOST = "account.bilibili.com";
	public static final String ACCOUNT_IP = DEFAULT_IP;
	public static final String MEMBER_HOST = "member.bilibili.com";
	public static final String MEMBER_IP = DEFAULT_IP;
	public static final String INTERFACE_HOST = "interface.bilibili.com";
	public static final String INTERFACE_IP = DEFAULT_IP;
	private static final String SPACE_HOST = "space.bilibili.com";
	private static final String SPACE_IP = DEFAULT_IP;

	public static class ReqBuilder {
		private String ip;
		private String host;

		private ReqBuilder(String ip, String host) {
			this.ip = ip;
			this.host = host;
		}

		public Req get(String path) {
			return Req.get( "http://" + ip + path ).header( "Host", host );
		}

		public Req post(String path) {
			return Req.post( "http://" + ip + path ).header( "Host", host );
		}
	}

	public static ReqBuilder api() {
		return new ReqBuilder( API_IP, API_HOST );
	}

	public static ReqBuilder account() {
		return new ReqBuilder( ACCOUNT_IP, ACCOUNT_HOST );
	}

	public static ReqBuilder interface0() {
		return new ReqBuilder( INTERFACE_IP, INTERFACE_HOST );
	}

	public static ReqBuilder member() {
		return new ReqBuilder( MEMBER_IP, MEMBER_HOST );
	}

	public static ReqBuilder space() {
		return new ReqBuilder( SPACE_IP, SPACE_HOST );
	}

}
