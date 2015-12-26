package org.xzc.bilibili.autoactive;

import java.util.List;

public class Data1 {
	public long currentTime;
	public long endTime;
	public List<Question> questionList;
	@Override
	public String toString() {
		return "Data [currentTime=" + currentTime + ", endTime=" + endTime + ", questionList=" + questionList + "]";
	}
}
