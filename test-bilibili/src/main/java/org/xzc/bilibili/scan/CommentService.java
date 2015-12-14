package org.xzc.bilibili.scan;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;

@Component
public class CommentService {
	public boolean accept(Video v) {
		if (v.mid == 1643718) // up主是山下智博
			return true;
		if (( v.typeid == 71 || v.typeid == 138 ) && v.mid == 883968) // 暴走漫画
			return true;
		if (v.typeid == 15 && v.title.contains( "监狱学园" ))
			return true;
		//if (v.title.contains( "一拳" ) || v.title.contains( "一击" ))
		//	return false;
		//return v.typeid == 33 || v.typeid == 32 || v.typeid == 31;
		//return v.typeid == 32;
		return false;
	}

	private Random random = new Random();

	private char randomChar() {
		return (char) ( 'A' + random.nextInt( 26 ) );
	}

	private String 重复(char c) {
		int r = random.nextInt( 3 ) + 1;
		String s = "";
		for (int i = 0; i < r; ++i)
			s += c;
		return s;
	}

	public String getComment(Video v) {
		if (v.mid == 1643718) {
			// return "喂, 110, 这里有大绅(变)士(态), 请速速前来!";
			return "前些天大胆尝试举报山下君，竟然被大家讨厌了， 连号都封了， 丧心病狂啊这。。。";
		}
		if (( v.typeid == 71 || v.typeid == 138 ) && v.mid == 883968) {// 暴走漫画
			if (v.title.contains( "暴走大事件第四季" ))
				return "感谢大事件, 每周都给我们带来欢乐!~";
			if (v.title.contains( "暴走敖尼玛" ))
				return "每期的吐槽都好犀利啊!";
		}
		if (v.typeid == 15 && v.title.contains( "监狱学园" )) {
			return "每一集都做得很好啊, 剧情做得还是满紧凑的, 终于要到结尾了!!!";
		}
		//return "第一第一" + 重复( '!' ) + " 来支持一下视频" + 重复( '.' );
		//if (v.typeid == 32) {//完结动画
		//return "没有人评论吗? 新番还没看完, 旧的又有得看了.";
		//	return "这, 好古老的番啊.";
		//}
		return null;
	}
}
