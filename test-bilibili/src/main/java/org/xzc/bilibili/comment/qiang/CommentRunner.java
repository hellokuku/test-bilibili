package org.xzc.bilibili.comment.qiang;

import java.text.SimpleDateFormat;

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
		Config c0 = new Config( 1, "tag", "61.164.47.167", "19997766", "f3e878e5,1451143184,7458bb46",
				"454ba9153a48adeb7fc170806aadbd2c",
				512, 1000, true, 0, "msg", null, null );
		/*
		addJob( s, commentJob, c0.custom( "测试测试", 3381920, "坐等12集更新.",
				new DateTime().plusSeconds( 0 ).toDate(),
				new DateTime().plusSeconds( 122 ).toDate() ).setStopWhenForbidden( false ).setBatch( 1 )
				.setInterval( 1 ) );
		*/
		addJob( s, commentJob, c0.custom( "一拳超人", 3407473, "测试测试",
				new DateTime().plusSeconds( 0 ).toDate(),
				new DateTime().plusSeconds( 36000 ).toDate() ).setBatch( 4 ).setInterval( 1 ) );

		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}
}