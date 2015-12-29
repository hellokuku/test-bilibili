package org.xzc.vcode;

import java.nio.charset.Charset;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class Position implements IPosition {
	
	private static final Logger log = Logger.getLogger( Position.class );
	private static final Charset UTF8 = Charset.forName( "utf8" );
	private IWorker worker;
	private String hint;
	private String tag;

	public Position(String tag) {
		this.tag = tag;
	}

	public void bindWorker(IWorker worker) {
		this.worker = worker;
		byte[] data = worker.getVCodeData();
		hint = new String( data, UTF8 );
	}

	public void process() {
		log.info( "为 " + worker.getTag() + " 输入验证码" );
		System.out.println( "请输入验证码, 提示=" + hint );
		Scanner scanner = new Scanner( System.in );
		String yzm = scanner.nextLine();
		worker.doAfter( yzm );
	}

	public void unbindWorker() {
		worker = null;
	}

	public String getTag() {
		return tag;
	}

	public void reject() {
		worker.onReject();
	}

}
