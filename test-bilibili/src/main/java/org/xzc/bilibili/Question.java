package org.xzc.bilibili;

public class Question {
	public String qs_id;
	public String ans1;
	public String ans2;
	public String ans3;
	public String ans4;
	public String question;

	@Override
	public String toString() {
		return "Question [qs_id=" + qs_id + ", ans1=" + ans1 + ", ans2=" + ans2 + ", ans3=" + ans3 + ", ans4=" + ans4
				+ ", question=" + question + ", ans1_hash=" + ans1_hash + ", ans2_hash=" + ans2_hash + ", ans3_hash="
				+ ans3_hash + ", ans4_hash=" + ans4_hash + "]";
	}

	public String ans1_hash;
	public String ans2_hash;
	public String ans3_hash;
	public String ans4_hash;

	public String myAns;//我自己加的答案
	public int status;//0表示还没有回答过 1表示上一次回答了1但是错了 5表示已经正确了
}
