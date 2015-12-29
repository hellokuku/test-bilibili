package org.xzc.sina.email.auto;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.xzc.http.HC;
import org.xzc.http.Req;

public class Step2Worker {
	public final HC hc;
	public final File vcode;
	public final ExecutorService es;

	public Step2Worker(HC hc, File vcode, ExecutorService es) {
		this.hc = hc;
		this.vcode = vcode;
		this.es = es;
	}

	public void downloadVCodeAsync() throws IOException {
		es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				FileUtils.writeByteArrayToFile( vcode, hc.getAsByteArray( "https://account.bilibili.com/captcha" ) );
				return null;
			}
		} );
	}

	private static String URL = "https://account.bilibili.com/register/mail";

	public void doAfter(final String email, final String yzm, final Step2Callback cb) throws IOException {
		es.submit( new Callable<Void>() {
			public Void call() throws Exception {
				String content = hc.getAsString( URL );
				Req req = Req.post( URL ).datas( "uname", email, "yzm", yzm, "agree", 1 );
				content = hc.asString( req );
				downloadVCodeAsync();
				cb.onResult( content, email );
				return null;
			}
		} );
	}

}
