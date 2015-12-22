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
import org.xzc.bilibili.comment.qiang.select.BestProxySelector;
import org.xzc.bilibili.comment.qiang.select.ProxyExecutionResult;
import org.xzc.bilibili.comment.qiang.select.ProxyFilter;

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
					"202.120.38.17:2076",
					"202.120.17.158:2076",
					"183.224.171.150:2076",

					/*
					 * old
					"218.191.30.12:80",
					"199.168.148.150:10059",
					"112.5.253.82:8081",
					"120.194.222.212:8000",
					"59.41.239.13:9797",
					"121.8.69.107:9797",
					"124.115.211.30:9999",
					"27.205.89.229:9999",
					"121.33.221.67:9797",
					"190.98.162.22:8080",
					"198.169.246.30:80",
					"220.233.213.38:8080",
					"123.49.33.252:8080",
					"202.195.192.197:3128",
					"120.24.248.225:8080",
					"122.225.107.70:8080",
					"218.213.166.218:81"
					*/
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
					"121.8.170.53:9797"
			/*
								"222.73.173.169:808",
								"58.218.198.61:808",
								"58.251.47.101:8081",
								"60.13.8.225:8888",
								"120.24.248.225:8080",
								"222.73.173.169:808",
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
								"121.8.170.53:9797",
								"27.205.89.229:9999" */ );
			Config c0 = c00.custom().setMode( 1 ).setSip( "61.164.47.167" )
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
			}*/
			//System.out.println( result );
			addJobs( s, commentJob, c0 );
		}
		/*
		 */
		System.out.println( "现在的时间是 " + DateTime.now().toString( "yyyy年MM月dd日 HH时mm分ss秒" ) );
	}

	private static final void addJobs(Scheduler s, JobDetail commentJob, Config c0) throws SchedulerException {
		addJob( s, commentJob, c0.custom( "亚里亚12", 3431356, c0.getMode() == 0 ? "水果忍者在哪里!?" : "多吃水果有益健康.",
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