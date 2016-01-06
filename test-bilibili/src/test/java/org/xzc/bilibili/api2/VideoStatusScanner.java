package org.xzc.bilibili.api2;

import org.xzc.bilibili.util.Utils;

public abstract class VideoStatusScanner implements Runnable {
	protected final String tag;
	protected final BilibiliService3 bs;
	protected final int aid;
	protected int count;

	public VideoStatusScanner(String tag, BilibiliService3 bs, int aid) {
		this.tag = tag;
		this.bs = bs;
		this.aid = aid;
	}

	public void run() {
		while (true) {
			try {
				if (run0())
					break;
			} catch (Exception e) {
				e.printStackTrace();
			}
			Utils.sleep( 1000 );
		}
	}

	protected abstract boolean run0() throws Exception;

}
