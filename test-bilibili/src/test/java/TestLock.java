import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestLock {
	private Object obj = new Object();
	private int x = 0;

	@Test
	public void testLock() throws InterruptedException {
		ExecutorService es = Executors.newFixedThreadPool( 4 );
		for (int i = 0; i < 5; ++i) {
			es.submit( new Callable<Void>() {
				public Void call() throws Exception {
					Thread.sleep( 4000 );
					System.out.println( "结束" );
					return null;
				}
			} );
		}
		es.shutdown();
		es.awaitTermination( 1, TimeUnit.HOURS );
	}
}
