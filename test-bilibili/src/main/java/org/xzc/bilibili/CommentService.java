package org.xzc.bilibili;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;

@Component
public class CommentService {
	public boolean accept(Video v) {
		if (v.mid == 1643718) //up主是山下智博
			return true;
		//if (v.typeid == 30) {
		//	return v.title.contains( "初音" ) || v.title.contains( "洛天依" );
		//}
		if (v.typeid == 71 && v.mid == 883968) //暴走漫画
			return true;
		return v.typeid == 153 || /*v.typeid == 31 ||*/v.typeid == 32 || v.typeid == 33; /*|| v.typeid == 17 || v.typeid == 65;*/
	}

	private Random random = new Random();

	private char randomChar() {
		return (char) ( 'A' + random.nextInt( 26 ) );
	}

	public String getComment(Video v) {
		if (v.mid == 1643718) {
			//return "喂, 110, 这里有大绅(变)士(态), 请速速前来!";
			return "前两天尝试举报山下君，结果被大家讨厌了。";
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
		if (v.typeid == 153) {//国产动画
			if (v.title.contains( "狐妖小红娘" ))
				return "上一次看的时候是第一集的时候。。。";
			if (v.title.contains( "那年那兔那些事儿" ))
				return "看的时候总想说点什么，想想还是不说了。";
			return "国产加油啊，路还很长啊。";
		}
		return "喝大力, 必须拿第一! " + randomChar();
	}
}
