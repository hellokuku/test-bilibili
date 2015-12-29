package org.xzc.sina.email.auto;

import java.io.IOException;

public interface Step1Callback {
	public void onResult(Step1Worker step1Worker, int status)throws IOException ;
}
