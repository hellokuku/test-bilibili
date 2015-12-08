package org.xzc.bilibili.task;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "commentTask")
public class CommentTask {
	@DatabaseField(id = true)
	public int aid;
	@DatabaseField()
	public String msg;
	@DatabaseField
	public int status;

	public CommentTask() {
	}

	public CommentTask(int aid, String msg) {
		this.aid = aid;
		this.msg = msg;
	}
}
