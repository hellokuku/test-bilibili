package org.xzc.bilibili.autosignin;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "afasi")
public class AccountForAutoSignIn {
	@DatabaseField(id = true)
	public int mid;
	@DatabaseField
	@JSONField(name = "uname")
	public String name;
	@DatabaseField
	public int coins;
	@DatabaseField
	public String access_key;
	@DatabaseField(dataType = DataType.DATE_STRING)
	public Date updateAt;
	@Override
	public String toString() {
		return "AccountForAutoSignIn [mid=" + mid + ", name=" + name + ", coins=" + coins + ", access_key=" + access_key
				+ ", updateAt=" + updateAt + "]";
	}
}
