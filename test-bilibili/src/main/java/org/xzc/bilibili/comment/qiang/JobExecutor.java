package org.xzc.bilibili.comment.qiang;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.xzc.bilibili.comment.qiang.select.ProxyExecutionResult;

public class JobExecutor {
	private Config cfg;

	public JobExecutor(Config cfg) {
		this.cfg = cfg;
	}

	public void executor() {
		AtomicBoolean stop = new AtomicBoolean( false );
		AtomicLong last = new AtomicLong( 0 );
		System.out.println( "开始执行 " + cfg );
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
			//等待其他代理
			for (CommentWoker th : threadList)
				try {
					th.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			resultList = new ArrayList<ProxyExecutionResult>();
			threadList.add( t );
			total = 0;
			//统计信息
			for (CommentWoker th : threadList) {
				ProxyExecutionResult r = new ProxyExecutionResult();
				r.ip = th.getProxyHost();
				r.port = th.getProxyPort();
				r.count = th.getCount();
				total += r.count;
				resultList.add( r );
			}
		}
	}

	private int total = 0;

	public void printResult() {
		for (ProxyExecutionResult r : resultList) {
			System.out.println(
					String.format( "[%s] [%s] count=%d", cfg.getTag(), ( r.ip == null ? "本机" : r.ip ), r.count ) );
		}
		System.out.println( String.format( "[%s] %d台 total=%d", cfg.getTag(), resultList.size(), total ) );
	}

	private List<ProxyExecutionResult> resultList;

	public List<ProxyExecutionResult> getResultList() {
		return resultList;
	}
}
