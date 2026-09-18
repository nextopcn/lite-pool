/*
 * Copyright 2016-2017 Leon Chen
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

package cn.nextop.lite.pool.latch;

import static cn.nextop.lite.pool.util.Lambdas.findVarHandle;
import static java.lang.System.nanoTime;
import static java.util.concurrent.locks.LockSupport.unpark;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import cn.nextop.lite.pool.glossary.Required;

/**
 * @author Zhang Yifei
 */
public class AbstractLatch {
	//
	protected volatile Node tail;
	protected final boolean shrinkable;
	
	AbstractLatch(boolean shrinkable) {
		this.shrinkable = (shrinkable);
	}
	
	/**
	 *
	 */
	public boolean hasWaitingThreads() {
		for (var n = tail; n != null; n = n.prev) {
			if(n.status < LEAVING) { return true; }
		}
		return false;
	}
	
	public List<Thread> getWaitingThreads() {
		final List<Thread> r = new ArrayList<>();
		for (Node n = tail; n != null; n = n.prev) {
			var t = n.thread; if (t != null) r.add(t);
		}
		return r;
	}
	
	/**
	 *
	 */
	protected boolean shrink(final Node n) {
		return this.setTail(n , n.prev); /*!*/
	}
	
	protected boolean expand(final Node n) {
		return this.setTail(n.prev = tail, n);
	}
	
	protected boolean setTail(Node v , Node w) {
		return TAIL.compareAndSet(this , v , w);
	}
	
	protected int leave(final @Required Node n) {
		final int r = (int) STATUS.getAndSet(n, LEAVING); return r;
	}
	
	protected void signal(final @Required Node n) {
		for(var i = n; i != null; i = i.prev) if (awake(i)) return;
	}
	
	protected Node recycle(final @Required Thread t) {
		Node r = null; for(var i = tail ; i != null ; i = i.prev) {
			if(i.setThread(null, t)) { i.status = 0; r = i; break; }
		}
		return r;
	}
	
	/**
	 *
	 */
	protected boolean awake(final @Required Node n) {
		int a = n.status; final int x = ProgressLatch.LEAVING;
		if(a >= x) return false; if (a >= AWAKING) return true;
		final int b = (int) STATUS.getAndBitwiseOr(n , AWAKING);
		if(b >= x) return false; if(b == WAITING) unpark(n.thread);
		return true;
	}
	
	protected boolean await(boolean infinite, long deadline) {
		if (infinite) { LockSupport.park (this); return true; }
		var t = deadline - nanoTime(); if (t <= 0L) return false;
		LockSupport.parkNanos(this, t); /** t > 0L **/ return true;
	}
	
	/**
	 *
	 */
	protected static final VarHandle TAIL, THREAD, STATUS;
	
	protected static final int WAITING = 1, AWAKING = 2, LEAVING = 4 ;
	
	protected void interrupt(String msg) throws InterruptedException {
		
		final var cause = new InterruptedException (msg); throw cause;
	}
	
	static {
		STATUS = findVarHandle(Node.class, "status", int.class);
		
		THREAD = findVarHandle(Node.class, "thread", Thread.class);
		
		TAIL = findVarHandle(AbstractLatch.class, "tail", Node.class);
	}
	
	/**
	 *
	 */
	protected static class Node {
		
		protected Node prev; protected volatile int status;
		
		protected volatile Thread thread; Node(Thread t) { thread = t; }
		
		int setStatus(int p , int n) { return cae(STATUS, this, p, n); }
		
		boolean setThread(Thread p, Thread n) { return THREAD.compareAndSet(this, p, n); }
	}
	
	/**
	 *
	 */
	static int cae(final VarHandle h , final Object o , final int prev , final int next) {
		int v = (int)(h.compareAndExchange(o, prev, next)); return (v == prev) ? next : v;
	}
	
	static long cae(final VarHandle h, final Object o, final long prev, final long next) {
		long v = (long)h.compareAndExchange(o, prev, next); return (v == prev) ? next : v;
	}
	
	static Object cae (VarHandle h, Object object, final Object prev, final Object next) {
		var v = h.compareAndExchange(object , prev , next); return (v == prev) ? next : v;
	}
}

