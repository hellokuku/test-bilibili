package org.xzc.bilibili.model;

/**
 * 对于大部分操作的结果 都有success, msg, status
 * @author xzchaoo
 *
 */
public class Result {

	/**
	 * 直接描述操作是否成功
	 */
	public final boolean success;

	/**
	 * 相关的信息
	 */
	public final String msg;

	/**
	 * 如果仅仅success不足以描述状态, 那么就新加一个status, 至于每个status的值对应什么意思, 那就要再说了... 
	 */
	public final int status;

	public Result(boolean success, String msg) {
		this( success, msg, 0 );
	}

	public Result(boolean success, String msg, int status) {
		this.success = success;
		this.msg = msg;
		this.status = status;
	}

	@Override
	public String toString() {
		return "Result [success=" + success + ", msg=" + msg + ", status=" + status + "]";
	}

}
