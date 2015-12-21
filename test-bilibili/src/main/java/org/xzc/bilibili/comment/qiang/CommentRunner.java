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

		//模式 tag 服务器ip
		//基于cookie的两个数据
		//基于api的两个字段
		//线程 间隔 禁言是否停止
		//aid 消息 开始时间 结束时间
		int mode = 0;
		if (mode == 0) {
			Config c0 = new Config( 0, "112.25.85.6",
					"19480366", "f3e878e5,1451143184,7458bb46", // xzchao xuzhichaoxh3@163.com
					"19997766", "454ba9153a48adeb7fc170806aadbd2c", // jzxcai bzhxh1@sina.com
					1024, 1000, true, true,
					"tag", 0, "msg", null, null );
			//for 0
			addJob( s, commentJob, c0.custom( "高校星歌剧12", 3426180, "还给我麦克风.",
					new DateTime( 2015, 12, 21, 23, 58, 0 ).toDate(),
					new DateTime( 2015, 12, 22, 0, 3, 20 ).toDate() ) );
			addJob( s, commentJob, c0.custom( "JK做饭", 3426189, "然而还没有完结.",
					new DateTime( 2015, 12, 22, 0, 3, 30 ).toDate(),
					new DateTime( 2015, 12, 22, 0, 12 ).toDate() ) );
			addJob( s, commentJob, c0.custom( "网球并不可笑嘛", 3426213, "语速如此之快, 完结撒花.",
					new DateTime( 2015, 12, 22, 0, 20 ).toDate(),
					new DateTime( 2015, 12, 22, 0, 25 ).toDate() ) );
			addJob( s, commentJob, c0.custom( "动画锻炼11", 3426217, "身体已经如此虚弱, 还是要看动画锻炼！EX.",
					new DateTime( 2015, 12, 22, 1, 3 ).toDate(),
					new DateTime( 2015, 12, 22, 1, 12 ).toDate() ) );
			addJob( s, commentJob, c0.custom( "一拳超人", 3407473, "测试测试测试测试",
					new DateTime().plusSeconds( 0 ).toDate(),
					new DateTime().plusSeconds( 122 ).toDate() ) );
		} else {
			Config c1 = new Config( 1, "61.164.47.167",
					"19480366", "f3e878e5,1451143184,7458bb46", // xzchao xuzhichaoxh3@163.com
					"19997766", "454ba9153a48adeb7fc170806aadbd2c", // jzxcai bzhxh1@sina.com
					8, 500, true, true,
					"tag", 0, "msg", null, null );
			//for 1
			addJob( s, commentJob, c1.custom( "高校星歌剧12", 3426180, "(＾－＾)V",
					new DateTime( 2015, 12, 21, 23, 58, 0 ).toDate(),
					new DateTime( 2015, 12, 22, 0, 3, 20 ).toDate() ) );
			addJob( s, commentJob, c1.custom( "JK做饭", 3426189, "\\(^o^)/~",
					new DateTime( 2015, 12, 22, 0, 3, 30 ).toDate(),
					new DateTime( 2015, 12, 22, 0, 12 ).toDate() ) );
			addJob( s, commentJob, c1.custom( "网球并不可笑嘛", 3426213, "\\(^o^)/YES!",
					new DateTime( 2015, 12, 22, 0, 13 ).toDate(),
					new DateTime( 2015, 12, 22, 0, 25 ).toDate() ) );
			addJob( s, commentJob, c1.custom( "动画锻炼11", 3426217, "Y(^o^)Y",
					new DateTime( 2015, 12, 22, 1, 3 ).toDate(),
					new DateTime( 2015, 12, 22, 1, 20 ).toDate() ) );
			addJob( s, commentJob, c1.custom( "一拳超人", 3407473, "测试测试测试测试",
					new DateTime().plusSeconds( 0 ).toDate(),
					new DateTime().plusSeconds( 10 ).toDate() ).setInterval( 100 ) );
		}
		/*
		 */
		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}
}