package org.xzc.bilibili.task;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "commentTask")
public class CommentTask {
	@DatabaseField(id = true)
	public int aid;
	@DatabaseField
	public int status;
	@DatabaseField(dataType = DataType.DATE_STRING)
	public Date updateAt;

	public CommentTask() {
	}

	public CommentTask(int aid) {
		this.aid = aid;
	}

	public CommentTask(int aid, int status) {
		this.aid = aid;
		this.status = status;
	}

}
