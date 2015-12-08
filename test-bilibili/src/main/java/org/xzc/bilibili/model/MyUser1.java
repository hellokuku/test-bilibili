package org.xzc.bilibili.model;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

public  class MyUser1 {
	@JSONField(name = "ID")
	public int id;
	public String name;
	@JSONField(serialize = false, deserialize = false)
	public String password;
	@JSONField(format = "yyyy--MM--dd", name = "bir")
	public Date birthday;
	@JSONField(name = "Age2")
	public int age;

	@Override
	public String toString() {
		return "MyUser1 [id=" + id + ", name=" + name + ", password=" + password + ", birthday=" + birthday
				+ ", age=" + age + "]";
	}
}