package org.xzc.bilibili.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 表示了一个账号的基本信息
 * @author xzchaoo
 *
 */
public class Account {
	private int id;//账号的id
	private String SESSIDATA;//用于cookie
	private String sex;
	private int fid;//默认的收藏夹的id
	private String name;//名称
	private boolean active;//是否激活
	private int coins;
	private int spacesta;
	@JSONField(name = "level_info")
	private LevelInfo levelInfo;
	public Account() {
	}
	public Account(int id, String SESSIDATA) {
		this.id = id;
		this.SESSIDATA = SESSIDATA;
	}

	public int getCoins() {
		return coins;
	}

	public int getFid() {
		return fid;
	}

	public int getId() {
		return id;
	}

	public LevelInfo getLevelInfo() {
		return levelInfo;
	}

	public String getName() {
		return name;
	}

	public String getSESSIDATA() {
		return SESSIDATA;
	}

	public String getSex() {
		return sex;
	}

	public int getSpacesta() {
		return spacesta;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCoins(int coins) {
		this.coins = coins;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLevelInfo(LevelInfo levelInfo) {
		this.levelInfo = levelInfo;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSESSIDATA(String sESSIDATA) {
		SESSIDATA = sESSIDATA;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public void setSpacesta(int spacesta) {
		this.spacesta = spacesta;
	}
	@Override
	public String toString() {
		return "Account [id=" + id + ", fid=" + fid + ", name=" + name + ", active=" + active + "]";
	}

}
