package org.xzc.bilibili.model;

import java.util.List;

public class FavGetList {
	public int mid;
	public int fid;
	public int pagesize;
	public int count;
	public int pages;
	public List<Video> vlist;
	@Override
	public String toString() {
		return "FavGetList [mid=" + mid + ", fid=" + fid + ", pagesize=" + pagesize + ", count=" + count + ", pages="
				+ pages + ", vlist=" + vlist + "]";
	}
}
