package org.xzc.bilibili.model;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xzc.bilibili.util.Sign;

public class TestSign {
	@Test
	public void test1() throws URISyntaxException, UnsupportedEncodingException {
		String url = "http://api.bilibili.com/view";
		Map<String, String> m = new HashMap<String, String>();
		//m.put( "userid", "8147532@qq.com" );
		//m.put( "pwd", "xzc@7086204511" );
		/*		m.put( "_device", "android" );
				m.put( "access_key", "8376371bad697b8bddb2e9aaa228a6fd" );
		
				m.put( "build", "408005" );
				m.put( "platform", "android" );
				m.put( "gourl", "http://www.bilibili.com/html/join.html" );*/
		//		m.put( "type", "xml" );
		//		m.put( "id", "919515" );
		//		m.put( "page", "1" );
		m.put( "id", "3356698" );
		m.put( "appkey", "c1b107428d337928" );
		/*		m.put( "_device", "android" );
				m.put( "_hwid", "3029927d46659793" );
				m.put( "_ulv", "5000" );
				m.put( "access_key", "76b5b9bdfe34bb8e2a809ba718ac5c6e" );
				m.put( "appkey", "c1b107428d337928" );
				m.put( "main_ver", "v2" );
				m.put( "platform", "android" );
				m.put( "playtag", "2634430" );
				m.put( "recommend_type", "related_post" );
				m.put( "rindex", "1" );*/
		Sign s = new Sign( m );
		System.out.println( new Sign( "appkey=c1b107428d337928&pwd=xzc@7086204511&userid=8147532@qq.com" ).getResult() );
		
	}
}
