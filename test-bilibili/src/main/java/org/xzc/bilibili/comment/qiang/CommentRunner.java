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
				.addCommentJob( "一拳超人", 3407473, "测试测试测试测试", DateTime.now(), DateTime.now().plusSeconds( 10 ) )
				.addCommentJob( "樱子小姐", 3436845, "樱子小姐, 完结撒花.", new DateTime( 2015, 12, 24, 0, 55 ),
						new DateTime( 2015, 12, 24, 1, 10 ) )
				.addCommentJob( "35", 3436851, "35小队, 完结撒花.", new DateTime( 2015, 12, 24, 1, 30 ),
						new DateTime( 2015, 12, 24, 1, 45 ) )
				.addCommentJob( "与魔共舞", 3436856, "与魔共舞, 完结撒花.", new DateTime( 2015, 12, 24, 1, 55 ),
						new DateTime( 2015, 12, 24, 2, 10 ) );
	}
}

/*
  		// 60.221.255.15 113.105.152.207 61.164.47.167 112.25.85.6 125.39.7.139
		//125.39.7.139 106.39.192.38  14.152.58.20 218.76.137.149 183.247.180.15
		CommentConfig c00 = new CommentConfig( 0, "ip",
				"19480366", "f3e878e5,1451143184,7458bb46", // xzchao xuzhichaoxh3@163.com
				"19997766", "454ba9153a48adeb7fc170806aadbd2c", // jzxcai bzhxh1@sina.com
				1, 1, true, true,
				"tag", 0, "msg", null, null );
		int mode = 2;
		if (mode == 0) {
			CommentConfig c0 = c00.custom().setMode( mode ).setSip( "61.164.47.167" )
					.setBatch( 1 ).setInterval( 1 );
			addJobs( s, commentJob, c0 );
		} else if (mode == 2) {
			CommentConfig c0 = c00.custom().setMode( mode ).setSip( "61.164.47.167" )
					.setBatch( 1 ).setInterval( 1 );
			addJobs( s, commentJob, c0 );
		} else {
			List<String> senderList = new ArrayList<String>( Arrays.asList(
					"202.120.38.17:2076",
					"202.120.17.158:2076",
					"183.224.171.150:2076",
					"199.168.148.150:10059",
					"112.5.253.82:8081",
					"59.41.239.13:9797",
					"121.8.69.107:9797",
					"121.33.221.67:9797",
					"190.98.162.22:8080",
					"198.169.246.30:80",
					"220.233.213.38:8080",
					"123.49.33.252:8080",
					"202.195.192.197:3128",
					"120.24.248.225:8080",
					"122.225.107.70:8080",
					"58.218.198.61:808",
					"60.13.8.225:8888",
					"120.24.248.225:8080",
					"58.218.198.61:808",
					"60.13.8.225:8888",
					"58.59.68.91:9797",
					"61.162.184.14:80",
					"101.226.12.223:80",
					"14.218.100.186:9797",
					"222.222.251.131:9999",
					"122.227.199.178:9797",
					"202.195.192.197:3128",
					"121.8.69.107:9797",
					"121.8.170.53:9797" ) );
			CommentConfig c0 = c00.custom().setMode( mode ).setSip( "61.164.47.167" )
					.setBatch( 32 ).setInterval( 500 ).setSenderList( senderList );
			/*List<String> result = new BestProxySelector( 10, new ProxyFilter() {
				public boolean accept(ProxyExecutionResult r) {
					return r.count >= 100;
				}
			} ).select( c0.custom( "一拳超人", 3407473, "测试测试测试测试",
					new DateTime().plusSeconds( 0 ).toDate(),
					new DateTime().plusSeconds( 12 ).toDate() ) );
			for (String str : result) {
				System.out.println( "\"" + str + "\"," );
			}
			//System.out.println( result );
			addJobs( s, commentJob, c0 );
		}
*/