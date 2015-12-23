package org.xzc.bilibili.comment.qiang;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.xzc.bilibili.comment.qiang.config.CommentJobConfig;

import com.alibaba.fastjson.JSON;

public class CommentJob implements Job {
	public static final String ARG_CONFIG = "config";

	public void execute(JobExecutionContext context) throws JobExecutionException {
		//恢复数据
		JobDataMap data = context.getTrigger().getJobDataMap();
		CommentJobConfig jobCfg = JSON.parseObject( data.getString( ARG_CONFIG ), CommentJobConfig.class );
		System.out.println( data.getString( ARG_CONFIG ) );
		JobExecutor je = new JobExecutor( jobCfg );
		je.execute();
		je.printResult();
	}
}
