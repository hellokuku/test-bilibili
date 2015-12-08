package org.xzc.bilibili.dao;

import java.util.List;

public class Page<T> {
	public int total;
	public int pagesize;
	public int page;
	public List<T> list;
}
