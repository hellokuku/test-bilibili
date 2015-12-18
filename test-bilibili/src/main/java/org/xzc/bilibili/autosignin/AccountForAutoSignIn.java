package org.xzc.bilibili.autosignin;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "afasi")
public class AccountForAutoSignIn {
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
	@Override
	public String toString() {
		return "AccountForAutoSignIn [mid=" + mid + ", name=" + name + ", coins=" + coins + ", userid=" + userid
				+ ", access_key=" + access_key + ", currentLevel=" + currentLevel + ", currentMin=" + currentMin
				+ ", currentExp=" + currentExp + ", nextExp=" + nextExp + "]";
	}


}
