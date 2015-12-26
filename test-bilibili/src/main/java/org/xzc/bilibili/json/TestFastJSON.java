package org.xzc.bilibili.json;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;

import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.xzc.http.HC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TestFastJSON {
	@Test
	public void testName2() throws Exception {
		JSONObject jo = JSON.parseObject( "{\"status\":false,\"data\":\"av\u53f7\u9519\u8bef\"}");
		String string = jo.getString( "data" );
		System.out.println( string );
	}

	public void testName() throws Exception {
		System.out.println( new SimpleDateFormat( "yyyy-MM-dd HH:mm" ).parse( "2015-12-05 21:20" ) );
		HC hc = new HC( HttpClients.custom().build() );
		String jsonStr = hc
				.getAsString( "http://space.bilibili.com/ajax/fav/getList?mid=19557513&pagesize=30&fid=19764585" );
		JSONObject json = JSON.parseObject( jsonStr );
		JSONArray ja = json.getJSONObject( "data" ).getJSONArray( "vlist" );
		for (int i = 0; i < ja.size(); ++i) {
			JSONObject jo = ja.getJSONObject( i );
			String create = jo.getString( "create" ) + ":00";
			jo.put( "create", create );
		}
		FavGetList2 list = JSON.toJavaObject( json, FavGetList2.class );
		System.out.println( list );
		hc.close();
	}
}
