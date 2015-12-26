package org.xzc.http;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class Req {
	private RequestBuilder rb;

	private Req(RequestBuilder rb) {
		this.rb = rb;
	}

	public static Req get(String url) {
		return new Req( RequestBuilder.get( url ) );
	}

	public static Req post(String url) {
		return new Req( RequestBuilder.post( url ) );
	}

	public Req addHeader(String name, Object value) {
		rb.addHeader( "name", value.toString() );
		return this;
	}

	public Req addHeader(Header header) {
		rb.addHeader( header );
		return this;
	}

	public Req addHeaders(Params p) {
		p.headersTo( rb );
		return this;
	}

	public Req addParam(String name, Object value) {
		rb.addParameter( name, value.toString() );
		return this;
	}

	public Req addParam(NameValuePair nvp) {
		rb.addParameter( nvp );
		return this;
	}

	public Req addParams(Params p) {
		p.paramsTo( rb );
		return this;
	}

	public Req addParams(Object... args) {
		new Params( args ).paramsTo( rb );
		return this;
	}

	public Req setDatas(Params p) {
		p.datasTo( rb );
		return this;
	}

	public Req setDatas(Object... args) {
		new Params( args ).datasTo( rb );
		return this;
	}

	public RequestBuilder getBuilder() {
		return rb;
	}

	public Req setHost(String host) {
		rb.addHeader( "Host", host );
		return this;
	}

	public HttpUriRequest build() {
		return rb.build();
	}
}
