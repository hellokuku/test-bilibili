package org.xzc.bilibili.model;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
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
	public int typeid = -1;
	@DatabaseField(dataType = DataType.DATE_STRING)
	public Date updateAt;
	@DatabaseField
	public int mid = -1;
	@DatabaseField
	public int status = -1;

	@JSONField(format = "yyyy-MM-dd HH:mm")
	public Date create;//收藏夹里的格式是2015-12-05 21:20

	public Video() {
	}

	public Video(int aid) {
		this.aid = aid;
	}

	@Override
	public String toString() {
		return "Video [aid=" + aid + ", title=" + title + ", typeid=" + typeid + ", updateAt=" + updateAt + ", mid="
				+ mid + ", status=" + status + ", create=" + create + "]";
	}

}
