package org.xzc.bilibili.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 表示了一个账号的基本信息
 * @author xzchaoo
 *
 */
@DatabaseTable(tableName = "account")
public class Account {
	@DatabaseField
	public int mid;

	@DatabaseField
	@JSONField(name = "uname")
	public String name;

	@DatabaseField
	public int coins;

	@DatabaseField(id = true)
	public String userid;

	@DatabaseField
	public String password;

	@DatabaseField
	public String access_key;

	@DatabaseField
	public int currentLevel;

	@DatabaseField
	public int currentMin;

	@DatabaseField
	public int currentExp;

	@DatabaseField
	public int nextExp;

	@DatabaseField
	public String SESSDATA;

	@DatabaseField
	public int fid;

	@Override
	public String toString() {
		return "[" + mid + ", " + name + ", " + userid + ", " + currentExp + "]";
	}

	public Account() {
	}

	public Account(String userid, String password) {
		this.userid = userid;
		this.password = password;
	}

	public Account(int mid, String SESSDATA) {
		this.mid = mid;
		this.SESSDATA = SESSDATA;
	}
}
