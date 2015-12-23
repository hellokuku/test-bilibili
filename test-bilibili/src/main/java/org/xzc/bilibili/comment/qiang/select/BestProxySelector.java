package org.xzc.bilibili.comment.qiang.select;

import java.util.List;

import org.xzc.bilibili.comment.qiang.JobExecutor;
import org.xzc.bilibili.comment.qiang.Proxy;
import org.xzc.bilibili.comment.qiang.config.CommentJobConfig;

public class BestProxySelector {
	private int count = 1;
	private ProxyFilter pf;

	public BestProxySelector(int count, ProxyFilter pf) {
		this.count = count;
		this.pf = pf;
	}

	public List<Proxy> select(CommentJobConfig jobCfg0) {
		CommentJobConfig jobCfg = jobCfg0.clone();
		List<Proxy> proxyList = jobCfg.getProxyList();
		for (int i = 0; i < count; ++i) {
			JobExecutor je = new JobExecutor( jobCfg );
			List<CommentResult> list = je.execute();
			//删除一些代理
			for (CommentResult r : list) {
				if (!pf.accept( r )) {
					proxyList.remove( r.getCommentConfig().getProxy() );
				}
			}
			jobCfg.setProxyList( proxyList );
		}
		return proxyList;
	}
}
