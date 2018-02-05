/*
 * Copyright 2016-2018 Nextop Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.lite.pool.util.concurrent.executor;

import cn.nextop.lite.pool.util.concurrent.thread.XThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Jingqi Xu
 */
public final class XExecutors {
	
	/**
	 * 
	 */
	public static XExecutorService create(String name, int threads) {
		return create(name, threads, null, null);
	}
	
	public static XExecutorService create(String name, int threads, boolean verbose) {
		XExecutorService r = create(name, threads, null, null); r.setVerbose(verbose); return r;
	}
	
	public static final XExecutorService[] creates(String name, int shards, int threads, ThreadFactory factory) {
		final XExecutorService r[] = new XExecutorService[shards];
		for(int i = 0; i < shards; i++) r[i] = XExecutors.create(name, threads, factory, null); return r;
	}
	
	/**
	 * 
	 */
	public static XExecutorService create(String name, int size, ThreadFactory factory) {
		return create(name, size, factory, null);
	}
	
	public static XExecutorService create(String name, int size, long alive, TimeUnit unit) {
		return create(name, size, size, alive, unit, null, null);
	}
	
	public static XExecutorService create(String name, int size, BlockingQueue<Runnable> queue) {
		return create(name, size, null, queue);
	}
	
	public static XExecutorService create(String name, int size, ThreadFactory factory, BlockingQueue<Runnable> queue) {
		return create(name, size, size, 0L, TimeUnit.MILLISECONDS, factory, queue);
	}
	
	public static XExecutorService create(String name, int size, int max, long alive, TimeUnit unit, ThreadFactory factory) {
		return create(name, size, max, alive, unit, factory, null);
	}
	
	public static XExecutorService create(String name, int size, int max, long alive, TimeUnit unit, ThreadFactory factory, BlockingQueue<Runnable> queue) {
		if(factory == null) factory = new XThreadFactory(name);
		if(queue == null) queue = new LinkedBlockingQueue<Runnable>();
		return new XThreadPoolExecutor(name, size, max, alive, unit, queue, factory);
	}
}
