package org.xzc.bilibili.proxy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * 各种代理的区别
 * http://blog.csdn.net/a19860903/article/details/47146715
 * @author xzchaoo
 *
 */
@Configuration
@ComponentScan
public class TestProxy {
	public static void main(String[] args) throws BeansException, Exception {
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext( TestProxy.class );
		ac.getBean( TestProxy.class ).run();
	}

	@Autowired
	private ProxyService ps;

	public void run() throws Exception {
		ps.directlyConnect();
		//doUpdate( ps.getProxyList() );
		//doUpdate( ps.getProxyList() );
		//doUpdate( proxyDao.queryForEq( "success", false ) );
		doUpdate( proxyDao.queryForEq( "success", true ) );
	}

	@Bean(destroyMethod = "close")
	public ConnectionSource connectionSource() {
		try {
			Class.forName( "org.sqlite.JDBC" );
			ConnectionSource cs = new JdbcConnectionSource( "jdbc:sqlite:bilibili.db" );
			return cs;
		} catch (Exception e) {
			throw new IllegalStateException( e );
		}
	}

	@Bean
	public RuntimeExceptionDao<Proxy, String> proxyDao(ConnectionSource cs) throws SQLException {
		TableUtils.createTableIfNotExists( cs, Proxy.class );
		RuntimeExceptionDao<Proxy, String> dao = new RuntimeExceptionDao( DaoManager.createDao( cs, Proxy.class ) );
		return dao;
	}

	@Autowired
	private RuntimeExceptionDao<Proxy, String> proxyDao;

	private void doUpdate(final List<Proxy> list) throws InterruptedException, ExecutionException {
		ExecutorService es = Executors.newFixedThreadPool( ps.getBatch() );
		List<Future<?>> futureList = new ArrayList<Future<?>>();
		System.out.println( "共有" + list.size() + "个" );
		int i = 0;
		for (Proxy p0 : list) {
			final int index = ++i;
			final Proxy p = p0;
			Future<Void> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					System.out.println( "处理第" + index + "个" );
					ps.tryProxy( p );
					p.setUpdateAt( new Date() );
					return null;
				}
			} );
			futureList.add( f );
		}
		for (Future<?> f : futureList)
			f.get();
		es.shutdown();
		proxyDao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				for (Proxy p : list) {
					proxyDao.createOrUpdate( p );
				}
				return null;
			}
		} );
	}

	//http://www.xicidaili.com/nn/1
	public void 列出所有代理() throws InterruptedException, ExecutionException {
		List<Proxy> list = ps.getProxyList();
		doUpdate( list );
	}
}
