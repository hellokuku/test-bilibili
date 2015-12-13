package org.xzc.bilibili.scan;

import java.util.List;

public class Page<T> {
	public int total;
	public int pagesize;
	public int page;
	public List<T> list;
}
