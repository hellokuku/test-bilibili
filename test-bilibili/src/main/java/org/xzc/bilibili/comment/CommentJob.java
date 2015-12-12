package org.xzc.bilibili.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
		Config c0 = JSON.parseObject( data.getString( ARG_CONFIG ), Config.class );
		List<Thread> threadList = new ArrayList<Thread>( c0.getSenderList().size() );
		//根据senderList创建多个子线程去工作
		try {
			AtomicBoolean stop = new AtomicBoolean( false );
			AtomicLong last = new AtomicLong( 0 );
			for (Sender s : c0.getSenderList()) {
				Config c1 = c0.copy().setSubTag( s.getTag() ).setBatch( s.getBatch() ).setProxyHost( s.getIp() )
						.setProxyPort( s.getPort() );
				CommentWokerThread t1 = new CommentWokerThread( c1, stop, last );
				t1.start();
				threadList.add( t1 );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Thread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println( c0.getTag() + "执行完毕!" );
	}
}
