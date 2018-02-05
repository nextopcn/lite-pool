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

import cn.nextop.lite.pool.util.Strings;
import cn.nextop.lite.pool.util.concurrent.thread.XThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Jingqi Xu
 */
public class XThreadPoolExecutor extends ThreadPoolExecutor implements XExecutorService {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(XThreadPoolExecutor.class);
	
	//
	private final String name;
	private boolean verbose = true;
	private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();
	
	/**
	 * 
	 */
	public XThreadPoolExecutor(String name, int size, int max) {
		this(name, size, max, new LinkedBlockingQueue<Runnable>());
	}
	
	public XThreadPoolExecutor(String name, int size, int max, ThreadFactory factory) {
		this(name, size, max, new LinkedBlockingQueue<Runnable>(), factory);
	}
	
	public XThreadPoolExecutor(String name, int size, int max, BlockingQueue<Runnable> queue) {
		this(name, size, max, queue, new XThreadFactory(name));
	}
	
	public XThreadPoolExecutor(String name, int size, int max, BlockingQueue<Runnable> queue, ThreadFactory factory) {
		this(name, size, max, 0L, TimeUnit.MILLISECONDS, queue, factory, new AbortPolicy());
	}
	
	public XThreadPoolExecutor(String name, int size, int max, long alive, TimeUnit unit, BlockingQueue<Runnable> queue, ThreadFactory factory) {
		this(name, size, max, alive, unit, queue, factory, new AbortPolicy());
	}
	
	public XThreadPoolExecutor(String name, int size, int max, long alive, TimeUnit unit, BlockingQueue<Runnable> queue, ThreadFactory factory, RejectedExecutionHandler handler) {
		super(size, max, alive, unit, queue, factory, handler); this.name = name;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return Strings.build(this)
		.append("name", name)
		.append("verbose", verbose).toString();
	}
	
	/**
	 * 
	 */
	@Override
	public void execute(Runnable command) {
		//
		RunnableFuture<?> rf = null;
		if(command instanceof RunnableFuture) { // Called by submit()
			rf = (RunnableFuture<?>)command;
		} else {
			rf = newTaskFor(command, null);
		}
		
		//
		try { notifyPrevEnqueue(rf); } finally { super.execute(rf); }
	}
	
	/**
	 * 
	 */
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isVerbose() {
		return verbose;
	}
	
	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	@Override
	public List<Listener> getListeners() {
		return this.listeners;
	}
	
	@Override
	public boolean addListener(Listener listener) {
		return this.listeners.add(listener);
	}
	
	@Override
	public boolean delListener(Listener listener) {
		return this.listeners.remove(listener);
	}
	
	@Override
	public void setListeners(List<Listener> listeners) {
		this.listeners.clear();
		if(listeners != null) this.listeners.addAll(listeners);
	}
	
	/**
	 * 
	 */
	@Override
	protected void terminated() { 
		super.terminated(); notifyOnTerminated();
	}
	
	@Override
	protected void beforeExecute(Thread t, Runnable runnable) {
		//
		super.beforeExecute(t, runnable);
		this.notifyPrevExecute((RunnableFuture<?>)runnable);
	}
	
	@Override
	protected void afterExecute(Runnable runnable, Throwable t) {
		//
		super.afterExecute(runnable, t);
		this.notifyPostExecute((RunnableFuture<?>)runnable, t);
	}
	
	/**
	 * 
	 */
	protected final void notifyOnTerminated() {
		if(this.listeners.size() == 0) return;
		for(final Listener listener : this.listeners) {
			try {
				listener.onTerminated(this);
			} catch(Throwable tx) {
				LOGGER.error("["+ getName() + "]failed to notify onTerminated, listener: " + listener, tx);
			}
		}
	}
	
	protected final void notifyPrevEnqueue(RunnableFuture<?> future) {
		if(this.listeners.size() == 0) return;
		for(final Listener listener : this.listeners) {
			try {
				listener.prevEnqueue(this, future);
			} catch(Throwable e) {
				LOGGER.error("["+ this.getName() + "]failed to notify prevEnqueue, listener: " + listener, e);
			}
		}
	}
	 
	protected final void notifyPrevExecute(RunnableFuture<?> future) {
		if(this.listeners.size() == 0) return;
		for(final Listener listener : this.listeners) {
			try {
				listener.prevExecute(this, future);
			} catch(Throwable tx) {
				LOGGER.error("["+ this.getName() + "]failed to notify prevExecute, listener: " + listener, tx);
			}
		}
	}
	
	protected final void notifyPostExecute(RunnableFuture<?> future, Throwable t) {
		if(this.listeners.size() == 0) return;
		for(final Listener listener : this.listeners) {
			try {
				listener.postExecute(this, future, t);
			} catch(Throwable tx) {
				LOGGER.error("["+ this.getName() + "]failed to notify postExecute, listener: " + listener, tx);
			}
		}
	}
}
