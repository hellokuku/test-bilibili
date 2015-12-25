package org.xzc.bilibili.api2;

public class Result<T> {
	public final boolean success;
	public final int status;
	public final int code;
	public final String msg;
	public final String content;
	public final T result;

	public Result(boolean success, int status, int code, String msg, String content, T result) {
		this.success = success;
		this.status = status;
		this.code = code;
		this.msg = msg;
		this.content = content;
		this.result = result;
	}

	public Result(boolean success) {
		this( success, 0, 0, null, null, null );
	}

	public Result(boolean success, T t) {
		this( success, 0, 0, null, null, t );
	}

}
