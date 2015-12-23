package org.xzc.json;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestES {
	public static void main(String[] args) throws Exception {
		ExecutorService es = Executors.newFixedThreadPool( 4 );
		for (int i = 0; i < 4; ++i) {
			final int index = i;
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					Thread.sleep( 10000 );
					System.out.println( "OK" );
					return null;
				}
			} );
		}
		es.shutdownNow();
	}
}
