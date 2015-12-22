package org.xzc.bilibili.comment.qiang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.alibaba.fastjson.JSON;

public class CommentRunner {
	private static final SimpleDateFormat SDF = new SimpleDateFormat( "MM月dd日HH时mm分ss秒" );

	private static Trigger addJob(Scheduler s, JobDetail job, Config cfg) throws SchedulerException {
		Trigger t = TriggerBuilder.newTrigger().startAt( cfg.getStartAt() ).forJob( job )
				.usingJobData( CommentJob.ARG_CONFIG, JSON.toJSONString( cfg ) ).build();
		s.scheduleJob( t );
		System.out.println( "[" + cfg.getTag() + "] 将会于" + SDF.format( cfg.getStartAt() ) + "开始, 于"
				+ SDF.format( cfg.getEndAt() ) + "结束." );
		return t;
	}

	public static void main(String[] args) throws Exception {
		StdSchedulerFactory f = new StdSchedulerFactory( "quartz2.properties" );
		Scheduler s = f.getScheduler();
		s.start();
		JobDetail commentJob = JobBuilder.newJob( CommentJob.class ).withIdentity( "comment" ).storeDurably().build();
		s.addJob( commentJob, false );
		// 60.221.255.15 113.105.152.207 61.164.47.167 112.25.85.6 125.39.7.139
		//125.39.7.139 106.39.192.38  14.152.58.20 218.76.137.149 183.247.180.15

		//模式 tag 服务器ip
		//基于cookie的两个数据
		//基于api的两个字段
		//线程 间隔 禁言是否停止
		//aid 消息 开始时间 结束时间
		Config c00 = new Config( 0, "61.164.47.167",
				"19480366", "f3e878e5,1451143184,7458bb46", // xzchao xuzhichaoxh3@163.com
				"19997766", "454ba9153a48adeb7fc170806aadbd2c", // jzxcai bzhxh1@sina.com
				1, 1, true, true,
				"tag", 0, "msg", null, null );
		int mode = 1;
		if (mode == 0) {
			Config c0 = c00.custom().setMode( 0 ).setSip( "112.25.85.6" )
					.setBatch( 1024 ).setInterval( 1000 );
			//for 0
			addJob( s, commentJob, c0.custom( "一拳超人", 3407473, "测试测试测试测试",
					new DateTime().plusSeconds( 0 ).toDate(),
					new DateTime().plusSeconds( 60 ).toDate() ) );
		} else {
			List<String> senderList = Arrays.asList(
					/*"113.240.246.165:1209",
					"118.163.165.250:3128",
					"120.24.248.225:8080",
					"121.41.93.201:808",
					"121.42.220.79:8088",
					"122.114.48.173:8000",
					"122.225.107.70:8080",
					"125.64.5.3:8000",
					"183.224.171.150:2076",
					"202.120.17.158:2076",
					"202.120.38.17:2076",
					"218.213.166.218:81",
					"218.63.208.223:3128",
					"222.73.173.169:808",
					"58.218.198.61:808",
					"58.251.47.101:8081",
					"59.108.61.132:808",
					"59.78.160.244:8080",
					"60.13.8.225:8888",
					"60.18.164.46:63000",
					"60.190.252.29:808",
					"61.149.182.102:8080"*/
					);
			//61.164.47.167
			Config c1 = c00.custom().setMode( 1 ).setSip( "61.164.47.167" )
					.setBatch( 32 ).setInterval( 500 ).setSenderList( senderList );
			//for 1
			addJob( s, commentJob, c1.custom( "一拳超人", 3407473, "测试测试测试测试",
					new DateTime().plusSeconds( 0 ).toDate(),
					new DateTime().plusSeconds( 12 ).toDate() ) );
		}
		/*
		 */
		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}
}