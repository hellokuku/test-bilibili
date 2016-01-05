package org.xzc.bilibili.model;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.xzc.bilibili.util.Sign;
import org.xzc.http.Params;

public class TestSign {
	@Test
	public void test1() throws URISyntaxException, UnsupportedEncodingException {
		Sign s = new Sign( "mid=0&email=ykonyezecc@hainan.net&time=1451989514" );
		System.out.println( s.getSign() );
	}
}
