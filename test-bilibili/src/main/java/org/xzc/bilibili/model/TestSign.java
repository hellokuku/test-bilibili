package org.xzc.bilibili.model;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.xzc.bilibili.api.Params;
import org.xzc.bilibili.util.Sign;

public class TestSign {
	@Test
	public void test1() throws URISyntaxException, UnsupportedEncodingException {
		String url = "http://bangumi.bilibili.com/api/report_watch";
		Params ps = new Params( "appkey", Sign.appkey, "build", 0, "episode_id", 81849, "mid", 19557513, "platform",
				"web", "ts", 1450927595 );
		RequestBuilder rb = RequestBuilder.get( url );
		ps.paramsTo( rb );
		Sign.signTo( rb );
		//http://www.bilibili.com/api_proxy?app=bangumi&action=/report_watch&episode_id=81849
		//http://bangumi.bilibili.com/api/report_watch?appkey=84b739484c36d653&build=0&episode_id=81849&mid=19557513&platform=web&ts=1450928050&sign=9f5b236e1bc7a53cbbe04f2c3b374c22
		HttpUriRequest req = rb.build();
		//http://bangumi.bilibili.com/api/report_watch?appkey=84b739484c36d653&build=0&episode_id=81849&mid=19557513&platform=web&ts=1450927595&sign=82e2e0bb36f4706046382c18a1dc1733
		System.out.println( req.getURI().toString() );
	}
}
