package org.xzc.bilibili.scan;

import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;

@Component
public class CommentService {
	@Deprecated
	public boolean accept(Video v) {
		if (v.mid == 1643718) // up主是山下智博
			return true;
		if (( v.typeid == 71 || v.typeid == 138 ) && v.mid == 883968) // 暴走漫画
			return true;
		if (v.typeid == 15 && v.title.contains( "监狱学园" ))
			return true;
		if (v.mid == 5676753 && v.title.contains( "脑洞小剧场" )) {
			return true;
		}
		return false;
	}

	public String getComment(Video v) {
		if (v.mid == 1643718) {
			return "喂, 110, 这里有大绅(变)士(态), 请速速前来!";
		}
		if (( v.typeid == 71 || v.typeid == 138 ) && v.mid == 883968) {// 暴走漫画
			if (v.title.contains( "暴走大事件第四季" ))
				return "感谢大事件, 每周都给我们带来欢乐!~";
			if (v.title.contains( "暴走敖尼玛" ))
				return "每期的吐槽都好犀利啊!";
		}
		if (v.mid == 5676753 && v.title.contains( "脑洞小剧场" )) {
			return "UP主确实厉害啊， 每周都有得看。";
		}
		return null;
	}
}
