package org.xzc.bilibili.comment.qiang;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSON;

public class CommentJob implements Job {
	public static final String ARG_CONFIG = "config";

	public void execute(JobExecutionContext context) throws JobExecutionException {
		//恢复数据
		JobDataMap data = context.getTrigger().getJobDataMap();
		Config cfg = JSON.parseObject( data.getString( ARG_CONFIG ), Config.class );

		AtomicBoolean stop = new AtomicBoolean( false );
		AtomicLong last = new AtomicLong( 0 );

		if (cfg.getMode() == 0) {
			CommentWoker t = new CommentWoker( cfg, stop, last );
			t.run();
			System.out.println( String.format( "[%s] 本机 count=%d", cfg.getTag(), t.getCount() ) );
		} else {
			List<CommentWoker> threadList = new ArrayList<CommentWoker>();
			for (String sender : cfg.getSenderList()) {//跑代理
				String[] ss = sender.split( ":" );
				String proxyHost = ss[0];
				int proxyPort = Integer.parseInt( ss[1] );
				CommentWoker t = new CommentWoker( cfg, stop, last, proxyHost, proxyPort );
				t.start();
				threadList.add( t );
			}
			//本机运行
			CommentWoker t = new CommentWoker( cfg, stop, last );
			t.run();

			int total = t.getCount();

			//等待其他代理
			for (CommentWoker th : threadList)
				try {
					th.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			//统计信息
			for (CommentWoker th : threadList) {
				total += th.getCount();
				String proxyHost = th.getProxyHost();
				System.out.println( String.format( "[%s] [%s] count=%d", cfg.getTag(), proxyHost, th.getCount() ) );
			}
			System.out.println( String.format( "[%s] [%s] count=%d", cfg.getTag(), "本机", t.getCount() ) );
			System.out.println( String.format( "[%s] %d台 total=%d", cfg.getTag(), threadList.size() + 1, total ) );
			threadList.clear();
		}
	}
}
