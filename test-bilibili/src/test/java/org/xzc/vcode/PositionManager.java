package org.xzc.vcode;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.xzc.vcode.Decider.Result;

public class PositionManager implements IPositionManager {
	private static final Logger log = Logger.getLogger( PositionManager.class );
	private int batch = 1;
	private LinkedBlockingQueue<IPosition> unbindedPositionList = new LinkedBlockingQueue<IPosition>();

	private LinkedBlockingQueue<IPosition> bindedPositionList = new LinkedBlockingQueue<IPosition>();

	public PositionManager() {
	}

	public synchronized void bind(IWorker worker) {
		try {
			IPosition p = unbindedPositionList.take();
			p.bindWorker( worker );
			bindedPositionList.put( p );
		} catch (InterruptedException e) {
			throw new RuntimeException( e );
		}
	}

	public int getBatch() {
		return batch;
	}

	public void init() {
		for (int i = 0; i < batch; ++i)
			unbindedPositionList.add( new Position( "p" + i ) );
	}

	public void loop() throws Exception {
		loop( Decider.ACCEPT );
	}

	public void loop(Decider de) throws Exception {
		LOOP: while (true) {
			IPosition p = bindedPositionList.take();//拿到一个已经绑定的p
			log.info( "获得p " + p.getTag() );
			try {
				Result r = de.accept( p );
				switch (r) {
				case ACCEPT:
					p.process();//处理
					break;
				case REJECT:
					p.reject();//放弃
					break;
				case STOP:
					p.reject();
					break LOOP;
				}
			} finally {
				log.info( "解绑p " + p.getTag() );
				p.unbindWorker();
				unbindedPositionList.put( p );//将p解绑
			}
		}
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

}
