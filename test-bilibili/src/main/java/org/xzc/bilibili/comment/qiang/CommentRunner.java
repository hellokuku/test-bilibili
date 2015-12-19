package org.xzc.bilibili.comment.qiang;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

		List<Sender> senderList = new ArrayList<Sender>();
		//senderList.add( new Sender( "cache.sjtu.edu.cn", 8080, 32, "sjtu" ) );
		//senderList.add( new Sender( "202.120.17.158", 2076, 32, "158" ) );
		//		senderList.add( new Sender( "222.35.17.177", 2076, 16, "177" ) );
		//senderList.add( new Sender( "27.115.75.114", 8080, 16, "代理1" ) );//100
		//senderList.add( new Sender( "112.25.41.136", 80, 16, "代理2" ) );//100
		//下面的延迟大概都是200
		//senderList.add( new Sender( "120.52.73.11", 8080, 16, "代理3" ) );
		//以下延迟300
		//senderList.add( new Sender( "120.52.73.13", 8080, 16, "代理4" ) );
		//senderList.add( new Sender( "120.52.73.20", 8080, 16, "代理5" ) );
		//senderList.add( new Sender( "120.52.73.21", 80, 16, "代理6" ) );
		//senderList.add( new Sender( "120.52.73.24", 80, 16, "代理7" ) );
		//senderList.add( new Sender( "120.52.73.27", 80, 16, "代理8" ) );
		//senderList.add( new Sender( "120.52.73.29", 8080,16, "代理9" ) );
		//senderList.add( new Sender( "116.246.6.52", 80, 16, "代理10" ) );
		//senderList.add( new Sender( "122.72.33.139", 80, 16, "代理11" ) );
		//senderList.add( new Sender( "112.25.41.136", 80, 16, "代理12" ) );
		senderList.add( new Sender( 256, "本机" ) );
		addJob( s, commentJob, new Config(
				"一拳超人", 3407473, "测试测试",
				new DateTime().plusSeconds( 2 ).toDate(),
				new DateTime( 2015, 12, 19, 12, 0 ).toDate() ).setSenderList( senderList ) );
	}

}