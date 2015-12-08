package org.xzc.bilibili;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.xzc.bilibili.model.Video;

@Component
public class CommentService {
	public boolean accept(Video v) {
		if (v.mid == 1643718) //up主是山下智博
			return true;
		return v.typeid == 33 || v.typeid == 31 || v.typeid == 20 || v.typeid == 32||v.typeid==15;
	}
	private Map<Integer, String> 预定的文本 = new HashMap<Integer, String>();

	private Random random = new Random();

	private char randomChar() {
		return (char) ( 'A' + random.nextInt( 26 ) );
	}

	public String getComment(Video v) {
		String msg = 预定的文本.get( v.aid );
		if (msg != null)
			return msg;
		return "喝大力, 必须拿第一! " + randomChar();
	}
}
