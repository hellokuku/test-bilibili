package org.xzc.bilibili.model;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "video")
public class Video {
	@DatabaseField(id = true)
	public int aid;
	@DatabaseField
	public String title;
	@DatabaseField
	public int state = -1;
	@DatabaseField
	public int typeid = -1;
	@DatabaseField(dataType = DataType.DATE_STRING)
	public Date updateAt;
	@DatabaseField
	public int mid = -1;
	@DatabaseField
	public int status = -1;

	//暂时不考虑加入数据库
	public String pic;
	public String keywords;
	public String description;

	public boolean isOK() {
		return state == 0;
	}

	public boolean isMQX() {
		return state == 1;
	}

	public boolean isDeleted() {
		return state == 2;
	}

	public boolean notExists() {
		return state == 3;
	}

	public boolean isRedirect() {
		return state == 4;
	}

	@Override
	public String toString() {
		return "Video [aid=" + aid + ", title=" + title + ", state=" + state + ", typeid=" + typeid + ", updateAt="
				+ updateAt + ", mid=" + mid + ", status=" + status + "]";
	}

	public String getStateText() {
		switch (state) {
		case 0:
			return "正常";
		case 1:
			return "无权限";
		case 2:
			return "已删除";
		case 3:
			return "视频不存在";
		case 4:
			return "重定向";
		default:
			return "未知";
		}
	}
}
