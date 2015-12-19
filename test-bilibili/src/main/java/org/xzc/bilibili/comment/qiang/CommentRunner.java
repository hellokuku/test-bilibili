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
		senderList.add( new Sender( 512, "本机" ) );
		addJob( s, commentJob, new Config(
				"落第骑士", 3413825, "最后来一次深夜的一刀修罗. 完结撒花.",
				new DateTime( 2015, 12, 19, 22, 55 ).toDate(),
				new DateTime( 2015, 12, 19, 23, 30 ).toDate() ).setSenderList( senderList ) );
		addJob( s, commentJob, new Config(
				"KOWABON", 3413832, "每天吓吓更健康.",
				new DateTime( 2015, 12, 20, 0, 48 ).toDate(),
				new DateTime( 2015, 12, 20, 0, 55 ).toDate() ).setSenderList( senderList ) );
		addJob( s, commentJob, new Config(
				"点兔", 3413842, "请给我来点智乃.",
				new DateTime( 2015, 12, 20, 0, 57 ).toDate(),
				new DateTime( 2015, 12, 20, 1, 20 ).toDate() ).setSenderList( senderList ) );
		addJob( s, commentJob, new Config(
				"传颂之物", 3413848, "第一季才补完...",
				new DateTime( 2015, 12, 20, 1, 25 ).toDate(),
				new DateTime( 2015, 12, 20, 1, 50 ).toDate() ).setSenderList( senderList ) );
		addJob( s, commentJob, new Config(
				"炽天使", 3413854, "上一集看得太基动了.",
				new DateTime( 2015, 12, 20, 1, 55 ).toDate(),
				new DateTime( 2015, 12, 20, 2, 25 ).toDate() ).setSenderList( senderList ) );
		/*
				List<Sender> senderList2 = new ArrayList<Sender>();
				senderList2.add( new Sender( 1, "本机" ) );
				addJob( s, commentJob, new Config(
						"终物语", 3413894, "卡米亚的声音真心好听, 完结撒花.",
						new DateTime().toDate(),
						new DateTime( 2015, 12, 20, 10, 0 ).toDate() ).setSenderList( senderList2 ) );
		*/
		/*
		addJob( s, commentJob, new Config(
				"一拳超人1", 3407473, "测试测试",
				new DateTime().plusSeconds( 2 ).toDate(),
				new DateTime().plusSeconds( 256 ).toDate() ).setSenderList( senderList ) );
		
		addJob( s, commentJob, new Config(
				"一拳超人2", 3407473, "测试测试",
				new DateTime().plusSeconds( 5 ).toDate(),
				new DateTime().plusSeconds( 10 ).toDate() ).setSenderList( senderList ) );*/
	}

}