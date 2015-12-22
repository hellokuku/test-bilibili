package org.xzc.bilibili.comment.qiang;

import java.text.SimpleDateFormat;
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

	private static final Trigger addJob(Scheduler s, JobDetail job, Config cfg) throws SchedulerException {
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
		Config c00 = new Config( 0, "ip",
				"19480366", "f3e878e5,1451143184,7458bb46", // xzchao xuzhichaoxh3@163.com
				"19997766", "454ba9153a48adeb7fc170806aadbd2c", // jzxcai bzhxh1@sina.com
				1, 1, true, true,
				"tag", 0, "msg", null, null );
		int mode = 1;
		if (mode == 0) {
			Config c0 = c00.custom().setMode( 0 ).setSip( "112.25.85.6" )
					.setBatch( 1024 ).setInterval( 1000 );
			addJobs( s, commentJob, c0 );
		} else {
			List<String> senderList = Arrays.asList(
					/*"205.177.86.114:81",
					"201.202.246.162:8080",
					"14.161.5.13:808",
					"110.45.135.229:8080",
					"219.90.85.179:8080",
					"190.63.174.246:8081",
					"223.27.158.2:8080",
					"183.89.223.115:8080",
					"14.139.254.4:8080",
					"202.62.85.186:8080",
					"5.39.223.28:3128",
					"185.124.149.22:80",
					"40.118.131.11:8080",
					"31.184.242.44:8888",
					"136.243.193.182:3128",
					"213.208.177.124:3128",
					"106.187.54.102:8080",
					"190.82.90.226:3128",
					"194.44.213.62:3128",
					"191.101.56.31:8888",
					"81.30.69.3:80",
					"198.169.246.30:80",
					"190.98.162.22:8080",
					"181.39.23.86:8080",
					"1.0.243.128:8080",
					"116.66.201.46:8080",
					"154.64.209.238:8081",
					"118.97.201.92:8080",
					"36.74.159.86:8080",
					"202.43.183.100:3128",
					"125.24.143.96:8080",
					"118.98.216.86:8080",
					"125.24.125.12:8080",
					"182.160.125.18:8088",
					"183.87.117.33:80",
					"183.87.117.35:80",
					"82.154.101.107:8118",
					"138.36.186.210:8080",
					"180.183.176.214:8080",
					"223.27.158.10:8080",
					"188.165.141.151:80",
					"187.188.204.163:8080",
					"220.233.213.38:8080",
					"118.97.239.146:8080",
					"123.49.33.252:8080"*/
			"202.195.192.197:3128",
			"113.240.246.165:1209",
			"118.163.165.250:3128",
			"120.24.248.225:8080",
			"121.42.220.79:8088",
			"122.114.48.173:8000",
			"122.225.107.70:8080",
			"125.64.5.3:8000",
			"183.224.171.150:2076",
			"202.120.17.158:2076",
			//"202.120.38.17:2076",
			"218.213.166.218:81",
			"222.73.173.169:808",
			"58.218.198.61:808",
			"58.251.47.101:8081",
			"59.108.61.132:808",
			"60.13.8.225:8888",
			"60.18.164.46:63000",
			"61.149.182.102:8080"
			);
			Config c0 = c00.custom().setMode( 1 ).setSip( "61.164.47.167" )
					.setBatch( 32 ).setInterval( 500 ).setSenderList( senderList );
			addJobs( s, commentJob, c0 );
		}
		/*
		 */
		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}

	private static final void addJobs(Scheduler s, JobDetail commentJob, Config c0) throws SchedulerException {
		addJob( s, commentJob, c0.custom( "一家", 3431351, "我们这一家.",
				new DateTime( 2015, 12, 22, 18, 28 ).toDate(),
				new DateTime( 2015, 12, 22, 18, 40 ).toDate() ) );
		addJob( s, commentJob, c0.custom( "亚里亚12", 3431356, "水果忍者在哪里!?",
				new DateTime( 2015, 12, 22, 22, 28 ).toDate(),
				new DateTime( 2015, 12, 22, 22, 40 ).toDate() ) );
		
		//测试用 跑12秒足够了
		addJob( s, commentJob, c0.custom( "一拳超人", 3407473, "测试测试测试测试",
				new DateTime().plusSeconds( 0 ).toDate(),
				new DateTime().plusSeconds( 12 ).toDate() ) );
		
		//测试用
		/*
		addJob( s, commentJob, c0.custom( "45219", 45219, "测试测试测试测试1",
				new DateTime().plusSeconds( 0 ).toDate(),
				new DateTime().plusSeconds( 12 ).toDate() ) );
		*/
	}
}