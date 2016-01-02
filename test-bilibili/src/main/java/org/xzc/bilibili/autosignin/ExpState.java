package org.xzc.bilibili.autosignin;

public class ExpState {
	public boolean login;
	public boolean video;
	public boolean share;
	public int count;

	public ExpState() {
	}

	@Override
	public String toString() {
		return "[" + login + ", " + video + ", " + share + ", " + count + "]";
	}

	public void updateCount() {
		count += login ? 1 : 0;
		count += video ? 1 : 0;
		count += share ? 1 : 0;
	}

}
