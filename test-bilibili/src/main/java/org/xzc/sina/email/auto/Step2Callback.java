package org.xzc.sina.email.auto;

import java.io.IOException;

public interface Step2Callback {
	public void onResult(String content, String email) throws IOException;
}
