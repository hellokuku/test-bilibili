package org.xzc.bilibili;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;

@Component
public class CommentService {
	public boolean accept(Video v) {
		if (v.mid == 1643718) //up主是山下智博
			return true;
		if (v.typeid == 30) {
			return v.title.contains( "初音" ) || v.title.contains( "洛天依" );
		}
		if (v.typeid == 71 && v.mid == 883968) //暴走漫画
			return true;
		return v.typeid == 31 || v.typeid == 32 || v.typeid == 33 /*|| v.typeid == 17 || v.typeid == 65*/;
	}

	private Random random = new Random();

	private char randomChar() {
		return (char) ( 'A' + random.nextInt( 26 ) );
	}

	public String getComment(Video v) {
		if (v.mid == 1643718) {
			return "喂, 110, 这里有大绅(变)士(态), 请速速前来!";
		}
		if (v.typeid == 30) {
			if (v.title.contains( "初音" )) //126 人力VOCALOID
				return "公主殿下的评论由我来攻占, up主加油.";
			if (v.title.contains( "洛天依" )) //126 人力VOCALOID
				return "天依的评论由我来攻占, up主加油.";
		}
		if (v.typeid == 71 && v.mid == 883968) {//暴走漫画
			if (v.title.contains( "暴走大事件第四季" ))
				return "感谢大事件, 每周都给我们带来欢乐!~";
			if (v.title.contains( "暴走敖尼玛" ))
				return "每期的吐槽都好犀利啊!";
		}
		return "喝大力, 必须拿第一! " + randomChar();
	}
}
