package org.xzc.bilibili.comment.qiang.select;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.xzc.bilibili.comment.qiang.Config;
import org.xzc.bilibili.comment.qiang.JobExecutor;

public class BestProxySelector {
	private int count = 1;
	private ProxyFilter pf;

	public BestProxySelector(int count, ProxyFilter pf) {
		this.count = count;
		this.pf = pf;
	}

	public List<String> select(Config cfg) {
		List<String> senderList = new ArrayList<String>( cfg.getSenderList() );
		for (int i = 0; i < count; ++i) {
			cfg.setStartAt( DateTime.now().toDate() ).setEndAt( DateTime.now().plusSeconds( 12 ).toDate() );
			JobExecutor je = new JobExecutor( cfg );
			je.executor();
			je.printResult();
			List<ProxyExecutionResult> list = je.getResultList();
			//删除执行次数为0的代理
			for (ProxyExecutionResult r : list) {
				if (!pf.accept( r ))
					senderList.remove( r.ip + ":" + r.port );
			}
			cfg.setSenderList( senderList );
		}
		return senderList;
	}
}
