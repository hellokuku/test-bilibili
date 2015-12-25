package org.xzc.bilibili.api2.reply;

import org.xzc.bilibili.model.LevelInfo;

import com.alibaba.fastjson.annotation.JSONField;

public class Reply {
	public static class Content {
		public String message;
		public String ip;
		public int plat;
		public String device;
		public String version;
	}
	public static class Member {
		public int mid;
		public String uname;
		public String sex;
		public String sign;
		public String avatar;
		public String rank;
		public String DisplayRank;
		@JSONField(name = "level_info")
		public LevelInfo levelInfo;
	}
	public int rpid;
	public int oid;
	public int type;
	public int mid;
	public int root;
	public int parent;
	public int count;
	public int rcount;
	public int floor;
	public int state;
	public int ctime;
	public int like;
	public int action;

	public Member member;

	public Content content;
}
