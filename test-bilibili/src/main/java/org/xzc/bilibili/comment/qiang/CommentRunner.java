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
		//String sip = "60.221.255.15"; 113.105.152.207 61.164.47.167 112.25.85.6 125.39.7.139
		Config c0 = new Config( "tag", "61.164.47.167", "19480366", "f3e878e5,1451143184,7458bb46", 1024,
				1000, true,
				0, "msg", null, null );
		addJob( s, commentJob, c0.custom( "小森", 3420289, "小森,大谷,完结后快去领证.",
				new DateTime( 2015, 12, 20, 21, 59 ).toDate(),
				new DateTime( 2015, 12, 20, 22, 5 ).toDate() ) );
		addJob( s, commentJob, c0.custom( "超人幻想", 3420305, "还好还有一集, 哈哈.",
				new DateTime( 2015, 12, 20, 22, 29 ).toDate(),
				new DateTime( 2015, 12, 20, 22, 35 ).toDate() ) );
		//addJob( s, commentJob, c0.custom( "路西法", 3420299, "完结之后我的灵魂可以还给我吗?",
		//		new DateTime( 2015, 12, 20, 22, 29 ).toDate(),
		//		new DateTime( 2015, 12, 20, 22, 35 ).toDate() ) );
		addJob( s, commentJob, c0.custom( "箱根", 3420311, "箱根酱就是厉害,还好还有一集. 还可以再水一集.",
				new DateTime( 2015, 12, 21, 0, 4 ).toDate(),
				new DateTime( 2015, 12, 21, 0, 20 ).toDate() ) );
		/*
		addJob( s, commentJob, c0.custom( "一拳超人", 3407473, "测试测试",
				new DateTime().toDate(),
				new DateTime().plusSeconds( 10 ).toDate() ).setStopWhenForbidden( false ).setInterval( 1 ) );*/
		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}
}