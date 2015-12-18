package org.xzc.json;

import java.util.Date;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

public class TestFastJson {
	//@JSONType(ignores = { "password" })
	public static class User {
		@JSONField(name = "id2")
		public int id;
		public String name;
		@JSONField(name = "password", serialize = false)
		public String password;
		@JSONField(format = "yyyy-MM-dd HH:mm")
		public Date birthday;

		@Override
		public String toString() {
			return "User [id=" + id + ", name=" + name + ", password=" + password + ", birthday=" + birthday + "]";
		}
	}

	@Test
	public void test1() {
		User user = JSON.parseObject( "{id2:1,name:'xzc',password:'ceshi',birthday:'2015-12-17 00:00:00'}",
				User.class );
		System.out.println( user );
		System.out.println( JSON.toJSONString( user ) );
	}
}
