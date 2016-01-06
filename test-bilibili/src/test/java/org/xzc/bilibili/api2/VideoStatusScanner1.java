package org.xzc.bilibili.api2;

import org.joda.time.DateTime;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.util.Utils;

public class VideoStatusScanner1 extends VideoStatusScanner {

	public VideoStatusScanner1(String tag, BilibiliService3 bs, int aid) {
		super( tag, bs, aid );
	}

	protected boolean run0() throws Exception {
		long beg = System.currentTimeMillis();
		Video v = bs.getVideo( aid );
		long end = System.currentTimeMillis();
		if (v.status == 0) {
			System.out.println(
					DateTime.now().toString( Utils.DATETIME_PATTER ) + " " + tag + " aid=" + aid + " 状态为0" );
			return true;
		}
		if (++count == 10) {
			count = 0;
			System.out.println( tag + " 耗时=" + ( end - beg ) + " " + v );
		}
		return false;
	}

}
