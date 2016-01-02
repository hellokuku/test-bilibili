package org.xzc.bilibili.comment.qiang;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.comment.qiang.config.CommentJobConfig;
import org.xzc.bilibili.comment.qiang.impl1.CommentExecutor;
import org.xzc.bilibili.comment.qiang.impl1.CommentExecutorFactory;
import org.xzc.bilibili.comment.qiang.select.CommentResult;
import org.xzc.bilibili.util.Utils;

public class JobExecutor {
	private static final Logger log = Logger.getLogger( JobExecutor.class );

	private CommentJobConfig jobCfg;

	public JobExecutor(CommentJobConfig jobCfg) {
		this.jobCfg = jobCfg;
	}

	public List<CommentResult> execute() {
		AtomicBoolean stop = new AtomicBoolean( false );
		AtomicLong last = new AtomicLong( 0 );
		List<CommentExecutor> executorList = new ArrayList<CommentExecutor>();
		int mode = jobCfg.getMode();

		if (log.isDebugEnabled())
			log.debug( String.format( "[%s] [%s] 开始执行, 模式=%d", jobCfg.getTag(),
					DateTime.now().toString( Utils.DATETIME_PATTER ), mode ) );

		//if (mode == -1 || mode == 0 || mode == 2 || mode == 3) {
			CommentExecutor myExecutor = CommentExecutorFactory.createCommentExecutor( jobCfg,
					jobCfg.getCommentConfig().setTag( jobCfg.getTag() ), stop, last );
			if (log.isDebugEnabled())
				log.debug( jobCfg.getCommentConfig() );
			myExecutor.run();
			executorList.add( myExecutor );
		/*} else {
			if (jobCfg.getProxyList() != null)
				for (Proxy proxy : jobCfg.getProxyList()) {//跑代理
					CommentConfig cfg = jobCfg.getCommentConfig()
							.clone()
							.setProxy( proxy )
							.setTag( jobCfg.getTag() + ", " + proxy.toString() );
					CommentExecutor ce = CommentExecutorFactory.createCommentExecutor( jobCfg, cfg, stop, last );
					ce.start();
					executorList.add( ce );
				}
			CommentExecutor myExecutor = null;
			if (jobCfg.isSelf()) {
				myExecutor = CommentExecutorFactory.createCommentExecutor( jobCfg,
						jobCfg.getCommentConfig()
								.clone().setTag( jobCfg.getTag() + " 本机" ),
						stop,
						last );
				myExecutor.run();
			}
			for (CommentExecutor ce : executorList)
				try {
					ce.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			if (jobCfg.isSelf()) {
				executorList.add( myExecutor );
			}
		}*/
		if (log.isDebugEnabled())
			log.debug( String.format( "[%s] 执行完毕", jobCfg.getTag() ) );
		commentResultList = makeCommentResultList( executorList );
		return commentResultList;
	}

	private List<CommentResult> commentResultList;

	private List<CommentResult> makeCommentResultList(List<CommentExecutor> executorList) {

		List<CommentResult> ret = new ArrayList<CommentResult>();
		for (CommentExecutor ce : executorList) {
			CommentResult cr = new CommentResult();
			cr.setCommentConfig( ce.getCommentConfig() );
			cr.setCount( ce.getCount() );
			cr.setDiu( ce.getDiu() );
			ret.add( cr );
		}
		return ret;
	}

	public void printResult() {
		int total = 0;
		for (CommentResult cr : commentResultList) {
			total += cr.getCount();
			System.out.println( String.format( "[%s] [%s] count=%d, diu=%d", jobCfg.getTag(),
					cr.getCommentConfig().getTag(), cr.getCount(), cr.getDiu() ) );
		}
		System.out.println( String.format( "[%s] %d台 total=%d", jobCfg.getTag(), commentResultList.size(), total ) );
	}

	public List<CommentResult> getCommentResultList() {
		return commentResultList;
	}
}
