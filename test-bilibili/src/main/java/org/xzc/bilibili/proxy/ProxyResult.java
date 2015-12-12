package org.xzc.bilibili.proxy;

public class ProxyResult {
	private boolean success;
	private long duration;

	@Override
	public String toString() {
		return "ProxyResult [success=" + success + ", duration=" + duration + "]";
	}

	public ProxyResult(boolean success, long duration) {
		super();
		this.success = success;
		this.duration = duration;
	}

	public ProxyResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

}
