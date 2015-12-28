package org.xzc.bilibili.autoactive;

public class Question {
	public String question;
	public String qs_id;
	public String ans1;
	public String ans2;
	public String ans3;
	public String ans4;
	public String ans1_hash;
	public String ans2_hash;

	@Override
	public String toString() {
		return question + "(" + qs_id + ")\n"
				+ ans1 + "(" + ans1_hash + ")\n"
				+ ans2 + "(" + ans2_hash + ")\n"
				+ ans3 + "(" + ans3_hash + ")\n"
				+ ans4 + "(" + ans4_hash + ")\n";
	}

	public String ans3_hash;
	public String ans4_hash;

	public String myAns;//我自己加的答案
	public int status;//0表示还没有回答过 1表示上一次回答了1但是错了 5表示已经正确了
}
