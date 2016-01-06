package org.xzc.bilibili.api2;

import org.joda.time.DateTime;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.model.FavGetList;
import org.xzc.bilibili.model.Video;
import org.xzc.bilibili.util.Utils;

public class VideoStatusScanner4 extends VideoStatusScanner {
	private final Account a;

	public VideoStatusScanner4(String tag, BilibiliService3 bs, int aid, Account a) {
		super( tag, bs, aid );
		this.a = a;
	}

	protected boolean run0() throws Exception {
		long beg = System.currentTimeMillis();
		int code = bs.addFavorite2( a, aid );
		FavGetList fvl = bs.getFavoriteList2( a );
		boolean result = bs.deleteFavoriteList2( a, fvl );
		long end = System.currentTimeMillis();
		Video video = null;
		for (Video v : fvl.vlist)
			if (v.aid == aid) {
				video = v;
				if (v.status == 0) {
					System.out.println(
							DateTime.now().toString( Utils.DATETIME_PATTER ) + " " + tag + " aid=" + aid
									+ " 状态为0" );
					return true;
				}
				break;
			}
		if (++count == 10) {
			count = 0;
			System.out.println( tag + " 耗时=" + ( end - beg ) + " " + video );
		}
		return false;
	}
}
