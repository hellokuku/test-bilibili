package org.xzc.bilibili.comment.qiang;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSON;

public class CommentJob implements Job {
	public static final String ARG_CONFIG = "config";

	public void execute(JobExecutionContext context) throws JobExecutionException {
		//恢复数据
		JobDataMap data = context.getTrigger().getJobDataMap();
		Config cfg = JSON.parseObject( data.getString( ARG_CONFIG ), Config.class );
		CommentWoker t1 = new CommentWoker( cfg );
		t1.run();
	}
}
