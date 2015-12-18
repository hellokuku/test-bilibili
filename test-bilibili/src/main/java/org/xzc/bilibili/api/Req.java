package org.xzc.bilibili.api;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class Req {
	private boolean get;
	private String url;
	private Params params = new Params();
	private Params datas = new Params();;

	private Req(boolean get, String url) {
		this.get = get;
		this.url = url;
	}

	public static Req get(String url) {
		return new Req( true, url );
	}

	public Params params() {
		return params;
	}

	public Params datas() {
		return datas;
	}

	public HttpUriRequest build() {
		RequestBuilder rb = null;
		if (get)
			rb = RequestBuilder.get( url );
		else
			rb = RequestBuilder.post( url );
		params.paramsTo( rb );
		if (!get)
			datas.datasTo( rb );
		return rb.build();
	}
}
