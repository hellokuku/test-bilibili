package org.xzc.bilibili;

public interface SafeRunner<T> {
	public T run() throws Exception;
}
