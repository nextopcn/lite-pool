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

package cn.nextop.lite.pool.util.concurrent.thread;

import cn.nextop.lite.pool.util.Maps;
import cn.nextop.lite.pool.util.Objects;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author Jingqi Xu
 */
public final class XThreadFactory implements ThreadFactory {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(XThreadFactory.class);
	
	//
	private final String name;
	private final AtomicBoolean daemon, tracked;
	private final List<WeakReference<Thread>> threads;
	private final ConcurrentHashMap<String, AtomicLong> sequences;
	private final AtomicReference<UncaughtExceptionHandler> handler;
	
	/**
	 * 
	 */
	public XThreadFactory() {
		this(null, false, null);
	}
	
	public XThreadFactory(String name) {
		this(name, false, null);
	}
	
	public XThreadFactory(String name, boolean daemon) {
		this(name, daemon, null);
	}
	
	public XThreadFactory(String name, boolean daemon, UncaughtExceptionHandler handler) {
		this.name = name;
		this.daemon = new AtomicBoolean(daemon);
		this.tracked = new AtomicBoolean(false);
		this.threads = new LinkedList<WeakReference<Thread>>();
		this.sequences = new ConcurrentHashMap<String, AtomicLong>();
		this.handler = new AtomicReference<UncaughtExceptionHandler>(handler);
	}
	
	/**
	 * 
	 */
	public String getName() {
		return name;
	}
	
	public boolean isDaemon() {
		return daemon.get();
	}
	
	public void setDaemon(boolean daemon) {
		this.daemon.set(daemon);
	}
	
	public boolean isTracked() {
		return tracked.get();
	}
	
	public void setTracked(boolean tracked) {
		this.tracked.set(tracked);
	}
	
	public List<Thread> getThreads(boolean alive) {
		return query(alive);
	}
	
	public final UncaughtExceptionHandler getHandler() {
		return this.handler.get();
	}
	
	public void setHandler(UncaughtExceptionHandler handler) {
		this.handler.set(handler);
	}
	
	/**
	 * 
	 */
	@Override
	public Thread newThread(final Runnable task) {
		//
		final Thread r = new XThread(task);
		r.setDaemon(isDaemon());
		if(this.isTracked()) track(r);
		
		//
		String prefix = this.name;
		if(prefix == null || prefix.equals("")) {
			prefix = getInvoker(2);
		}
		r.setName(prefix + "-" + getSequence(prefix));
		
		//
		UncaughtExceptionHandler handler = getHandler();
		if(handler != null) {
			r.setUncaughtExceptionHandler(handler);
		} else {
			r.setUncaughtExceptionHandler(new XHandler());
		}
		return r;
	}
	
	/**
	 * 
	 */
	private String getInvoker(final int depth) {
		final StackTraceElement[] stes = new Exception().getStackTrace();
		if(stes.length <= depth) {
			return getClass().getSimpleName();
		} else {
			return ClassUtils.getShortClassName(stes[depth].getClassName());
		}
	}
	
	private long getSequence(final String invoker) {
		AtomicLong r = this.sequences.get(invoker);
		if(r == null) {
			r = Maps.putIfAbsent(this.sequences, invoker, new AtomicLong(0));
		}
		return r.incrementAndGet();
	}
	
	/**
	 * 
	 */
	private synchronized void track(final Thread thread) {
		sweep(); this.threads.add(new WeakReference<Thread>(thread));
	}
	
	private synchronized void sweep() {
		for(Iterator<WeakReference<Thread>> i = threads.iterator(); i.hasNext(); ) {
			final Thread thread = i.next().get(); if(thread == null) i.remove();
		}
	}
	
	private synchronized List<Thread> query(final boolean alive) {
		final List<Thread> r = new LinkedList<Thread>();
		for(Iterator<WeakReference<Thread>> i = threads.iterator(); i.hasNext(); ) {
			final Thread thread = i.next().get(); if(thread == null) i.remove();
			else if(!alive || thread.isAlive()) r.add(thread);
		}
		return r;
	}
	
	/**
	 * 
	 */
	private static final class XHandler implements UncaughtExceptionHandler {
		public void uncaughtException(final Thread t, final Throwable tx) {
			LOGGER.error("unhandled exception: " + t.getId() + "@" + t.getName(), tx);
		}
	}
	
	private static final class XThread extends Thread implements FastThreadLocal.Aware {
		//
		private volatile Object cookie;
		protected XThread(Runnable runnable) { super(runnable); }
		
		//
		@Override public final <T> T getCookie() { return Objects.cast(this.cookie); }
		@Override public final void setCookie(Object cookie) { this.cookie = cookie; }
	}
}
