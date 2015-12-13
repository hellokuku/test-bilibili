package org.xzc.bilibili.json;

import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.xzc.bilibili.model.Account;
import org.xzc.bilibili.util.HC;

import com.alibaba.fastjson.JSON;

public class TestFastJSON {
	@Test
	public void testName() throws Exception {
		HC hc = new HC( HttpClients.custom().build() );
		String jsonStr = hc.getAsString( "http://api.bilibili.com/userinfo?mid=19216452" );
		Account a = JSON.parseObject( jsonStr, Account.class );
		System.out.println( JSON.toJSONString( a, true ) );
		hc.close();
	}
}
