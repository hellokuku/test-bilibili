package org.xzc.bilibili.reg;

import java.util.List;

import org.xzc.bilibili.Question;

public class Data {
	public long currentTime;
	public long endTime;
	public List<Question> questionList;
	@Override
	public String toString() {
		return "Data [currentTime=" + currentTime + ", endTime=" + endTime + ", questionList=" + questionList + "]";
	}
}
