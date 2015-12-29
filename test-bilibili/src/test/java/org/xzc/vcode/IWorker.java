package org.xzc.vcode;

public interface IWorker {
	public void doAfter(String yzm);//接着做

	public String getTag();//获得tag

	public byte[] getVCodeData();//获得验证码数据

	public void initAsync();//初始化该worker

	public void onReject();//该worker被拒绝了
	
}
