package org.xzc.bilibili.proxy;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
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
		//System.out.println( ps.getProxyList().size() );
		//ps.directlyConnect();
		//doUpdate( proxyDao.queryForEq( "success", false ) );
		doUpdate( proxyDao.queryForEq( "success", true ) );
		doUpdate( ps.getProxyList() );
	}

	@Test
	public void filter() {
		Set<String> set = new TreeSet<String>( Arrays.asList(
				"202.195.192.197:3128",
				"113.240.246.165:1209",
				"120.24.248.225:8080",
				"121.41.93.201:808",
				"121.42.220.79:8088",
				"122.225.107.70:8080",
				"125.64.5.3:8000",
				"182.90.13.116:80",
				"183.224.171.150:2076",
				"202.120.17.158:2076",
				"202.120.38.17:2076",
				"218.213.166.218:81",
				"218.63.208.223:3128",
				"222.73.173.169:808",
				"58.218.198.61:808",
				"58.251.47.101:8081",
				"59.108.61.132:808",
				"60.13.8.225:8888",
				"60.169.78.218:808",
				"60.18.164.46:63000",
				"61.149.182.102:8080" ) );
		for (String s : set) {
			System.out.println( "\"" + s + "\"" + "," );
		}
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
		for (Proxy p0 : list) {
			final Proxy p = p0;
			Future<Void> f = es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					ps.tryProxy( p );
					p.setUpdateAt( new Date() );
					return null;
				}
			} );
			futureList.add( f );
		}
		int index = 0;
		for (Future<?> f : futureList) {
			f.get();
			++index;
			if (index % 50 == 0)
				System.out.println( String.format( "%d/%d", index, futureList.size() ) );
		}
		es.shutdown();
		final int[] count = new int[] { 0 };
		proxyDao.callBatchTasks( new Callable<Void>() {
			public Void call() throws Exception {
				for (Proxy p : list) {
					proxyDao.createOrUpdate( p );
					if (p.isSuccess()) {
						System.out.println( String.format( "\"%s:%d\",", p.getIp(), p.getPort() ) );
						++count[0];
					}
				}
				return null;
			}
		} );
		System.out.println( count[0] );
	}

	//http://www.xicidaili.com/nn/1
	public void 列出所有代理() throws InterruptedException, ExecutionException {
		List<Proxy> list = ps.getProxyList();
		doUpdate( list );
	}
}
