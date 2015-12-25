package org.xzc.bilibili.proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.xzc.bilibili.comment.qiang.JobExecutor;
import org.xzc.bilibili.comment.qiang.config.CommentConfig;
import org.xzc.bilibili.comment.qiang.config.CommentJobConfig;
import org.xzc.bilibili.comment.qiang.Proxy;

public class BestProxySelectRunner {
	public static void main(String[] args) {
		// 60.221.255.15 113.105.152.207 61.164.47.167 112.25.85.6 125.39.7.139
		//125.39.7.139 106.39.192.38  14.152.58.20 218.76.137.149 183.247.180.15
		List<String> proxyStringList = new ArrayList<String>( Arrays.asList(
				"205.177.86.114:81",
				"183.88.103.195:8080",
				"82.144.204.150:3128",
				"40.118.131.11:8080",
				"219.90.85.179:8080",
				"180.183.121.66:8080",
				"14.161.5.13:808",
				"110.45.135.229:8080",
				"178.151.69.119:3128",
				"223.27.158.2:8080",
				"87.101.149.195:8080",
				"185.124.149.22:80",
				"195.58.245.66:3120",
				"190.213.106.218:3128",
				"218.30.35.86:8080",
				"31.184.242.248:8888",
				"136.243.193.182:3128",
				"198.169.246.30:80",
				"190.98.162.22:8080",
				"36.72.84.160:8080",
				"118.98.216.86:8080",
				"202.74.245.45:8080",
				"168.63.24.174:8118",
				"116.193.220.242:3128",
				"125.24.78.150:8080",
				"185.95.184.3:8080",
				"1.179.176.37:8080",
				"117.54.13.6:3128",
				"110.74.195.83:8080",
				"41.154.92.142:8080",
				"125.161.194.134:3128",
				"80.28.231.183:8080",
				"180.253.102.170:8080",
				"187.114.208.15:8080",
				"203.130.203.68:8080",
				"190.248.68.18:8080",
				"210.101.131.231:8080",
				"110.77.133.21:8080",
				"202.152.158.255:8080",
				"182.253.123.23:8080",
				"125.24.107.232:8080",
				"27.254.47.203:80",
				"187.60.40.113:8080",
				"180.250.182.50:8080",
				"5.62.128.165:8080",
				"180.73.180.106:81",
				"188.165.141.151:80",
				"195.90.181.138:8080",
				"46.101.167.103:8118",
				"203.130.212.162:8080",
				"37.236.160.201:8080",
				"90.154.127.19:8000",
				"125.24.107.232:8080",
				"199.168.148.150:10059",
				"185.101.230.114:8080",
				"94.77.161.176:8080",
				"1.179.176.37:8080",
				"178.33.214.57:8080",
				"85.185.238.214:8080",
				"178.33.201.95:8080" ) );
		List<Proxy> proxyList = new ArrayList<Proxy>();
		for (String s : proxyStringList) {
			String[] ss = s.split( ":" );
			proxyList.add( new Proxy( ss[0], Integer.parseInt( ss[1] ) ) );
		}
		//筛选出性能比较好的代理
		CommentJobConfig jobCfg = new CommentJobConfig()
				.setTag( "筛选代理" )
				.setMode(1 )
				.setSelf( false )
				.setStartAt( DateTime.now().toDate() )
				.setProxyList( proxyList )
				.setCommentConfig(
						new CommentConfig()
								.thread( 1, 1 )
								.api2( "19557477", "d89fa82e6dec663949f89abdf56d8ad1" )
								.video( 3407473, "测试用的消息", DateTime.now().plusSeconds( 30 ) ) );
		JobExecutor je = new JobExecutor( jobCfg );
		je.execute();
		je.printResult();
	}
}
