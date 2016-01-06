package org.xzc.bilibili.model;

import java.util.ArrayList;
import java.util.List;

public class FavGetList {
	public int count;
	public int pages;
	public List<Video> vlist = new ArrayList<Video>();

	@Override
	public String toString() {
		return "FavGetList [count=" + count + ", pages=" + pages + ", vlist=" + vlist + "]";
	}
}
