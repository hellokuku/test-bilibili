package org.xzc.bilibili.comment.qiang;

import java.text.SimpleDateFormat;
import java.util.Random;

import org.joda.time.DateTime;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.comment.qiang.config.CommentJobConfig;
import org.xzc.bilibili.util.Utils;

import com.alibaba.fastjson.JSON;

public class CommentRunner {
	private static final SimpleDateFormat SDF = new SimpleDateFormat( "MM月dd日HH时mm分ss秒" );
	private static Scheduler scheduler;
	private static JobDetail commentJob;

	private static final Trigger addJob(CommentJobConfig jobCfg0, CommentConfig cfg0, String tag, int aid, String msg,
			DateTime startAt, DateTime endAt) throws SchedulerException {

		CommentJobConfig jobCfg = jobCfg0.clone()
				.setTag( tag )
				.setStartAt( startAt.toDate() )
				.setCommentConfig( cfg0.clone().video( aid, msg, endAt ) );

		Trigger t = TriggerBuilder.newTrigger().startAt( startAt.toDate() ).forJob( commentJob )
				.usingJobData( CommentJob.ARG_CONFIG, JSON.toJSONString( jobCfg ) ).build();
		scheduler.scheduleJob( t );
		System.out.println( String.format( "[%s] 将会于 %s 开始, 于 %s 结束.", tag, startAt.toString( Utils.DATETIME_PATTER ),
				endAt.toString( Utils.DATETIME_PATTER ) ) );
		return t;
	}

	private static class TaskHelper {
		private CommentJobConfig jobCfg0;
		private CommentConfig cfg0;

		public TaskHelper(CommentJobConfig jobCfg0, CommentConfig cfg0) {
			this.jobCfg0 = jobCfg0;
			this.cfg0 = cfg0;
		}

		public TaskHelper addCommentJob(String tag, int aid, String msg, DateTime startAt, DateTime endAt)
				throws SchedulerException {
			addJob( jobCfg0, cfg0, tag, aid, msg, startAt, endAt );
			return this;
		}
	}

	public static void before() throws SchedulerException {
		StdSchedulerFactory f = new StdSchedulerFactory( "quartz2.properties" );
		scheduler = f.getScheduler();
		scheduler.start();
		commentJob = JobBuilder.newJob( CommentJob.class ).withIdentity( "comment" ).storeDurably().build();
		scheduler.addJob( commentJob, false );
	}

	public static void main(String[] args) throws Exception {
		before();
		CommentJobConfig jobCfg00 = new CommentJobConfig()
				.setMode( 2 );
		CommentConfig cfg00 = new CommentConfig()
				.thread( 512, 1000 )
				.setServerIP( "61.164.47.167" )
				.other( true, false )
				.setTimeout( 5000 )
				.cookie( "19480366", "f3e878e5,1451143184,7458bb46" );
		addJobs( jobCfg00, cfg00 );
		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}

	private static final void addJobs(CommentJobConfig jobCfg0, CommentConfig cfg0)
			throws SchedulerException {
		new TaskHelper( jobCfg0, cfg0 )
				.addCommentJob( "一拳超人", 3407473, "测试测试测试测试", DateTime.now(), DateTime.now().plusSeconds( 10 ) );
	}
}
