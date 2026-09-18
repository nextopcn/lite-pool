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
import static cn.nextop.lite.pool.util.Strings.build;
import static java.lang.System.nanoTime;
import static java.lang.Thread.interrupted;

import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;

import cn.nextop.lite.pool.glossary.Required;

/**
 * @author Zhang Yifei
 */
public class ProgressLatch64 extends AbstractLatch {
	//
	private volatile long progress;
	
	/**
	 *
	 */
	public ProgressLatch64() {
		this(0, false);
	}
	
	public ProgressLatch64(long p) {
		this(p, false);
	}
	
	public ProgressLatch64(long p, boolean s) {
		super(s); /* long */ this.progress = p;
	}
	
	/**
	 *
	 */
	@Override
	public String toString() {
		var p = get(); return build (this)
		.append("progress", p).toString();
	}
	
	/**
	 *
	 */
	public long get() { return progress; }
	
	public boolean advance(final long v) {
		long p; var h = PROGRESS; while(true) {
			p = progress; if (v <= p) return false;
			if (h.compareAndSet(this, p, v)) break;
		}
		this.signal (this.tail); /*!*/ return true;
	}
	
	public long getAndAdd(final @Required long v) {
		var r = (long) PROGRESS.getAndAdd(this, v);
		if(v > 0) this.signal(this.tail); return r;
	}
	
	public long getAndSet(final @Required long v) {
		var r = (long) PROGRESS.getAndSet(this, v);
		if(v > r) this.signal(this.tail); return r;
	}
	
	public long getAndOr (final @Required long v) {
		var r = (long) PROGRESS.getAndBitwiseOr(this, v);
		if ((v | r) > r) /* OR */ this.signal(this.tail); return r;
	}
	
	public boolean compareAndSet(long p , long v) {
		final var r = PROGRESS.compareAndSet(this, p, v);
		if (!r) return false; if (v > p) signal(tail); return true;
	}
	
	public long compareAndExchange(long p, long v) {
		var r = (long) PROGRESS.compareAndExchange( this , p , v );
		if(r == p && v > p) /*!*/ this.signal(this.tail); return r;
	}
	
	/**
	 *
	 */
	public void awaitUninterruptibly(final long v) {
		if (v >= this.progress) this.await(v , 0L , false);
	}
	
	public void await(long v) throws InterruptedException {
		if (Thread.interrupted()) throw new InterruptedException();
		if (v < this.progress || await(v , 0L , true) >= 0) return;
		interrupt ("await: " + v + ", progress: " + this.progress);
	}
	
	public boolean await (long v , final long t , final TimeUnit u)
			throws InterruptedException {
		
		if (Thread.interrupted()) throw new InterruptedException();
		if (v < progress) return true; if (t <= (0L)) return false;
		
		var nano = u.toNanos(t); var r = this.await(v, nano, true);
		if (r > 0) { return true ; } if (r == 0) { return false ; }
		interrupt ("await: " + v + ", progress: " + this.progress);
		return false;
	}
	
	/**
	 *
	 */
	private static final VarHandle PROGRESS; static {
		
		final Class<ProgressLatch64> clazz = ProgressLatch64.class;
		
		PROGRESS = findVarHandle (clazz , "progress" , long.class);
	}
	
	/**
	 *
	 */
	private int await(long value, long ns, boolean interruptible) {
		//
		var infinite = ns <= 0L; /* @see await */
		var deadline = infinite ? 0 : (nanoTime() + ns);
		Node node = null; int r = 0, s = 0, w = WAITING;
		boolean interrupted = false , expanding = false;
		
		for (;;) {
			if (value < this.progress) { r = 1; break; }
			if (node == (null)) { /* recycle | expand */
				final Thread t = Thread.currentThread();
				node = this.recycle(t); if(node == null) {
					expanding = !expand(node = new Node(t));
				}
			} else if (expanding) {
				expanding = !this.expand(node); /* expand */
			} else if (s >= AWAKING) {
				s = node.status = 0; this.signal(node.prev);
			} else if ((s = node.setStatus(0 , w)) == (w)) {
				if (!this.await(infinite, deadline)) { break; }
				s = node.setStatus(w , 0); var i = interrupted();
				if ((interrupted |= i) && interruptible) { break; }
			}
		}
		
		//
		if (node != null && !expanding) {
			s = leave(node); /* 1 leave, 2 shrink & 3 signal */
			if (!shrinkable || !shrink(node)) node.thread = null;
			if (s >= AbstractLatch.AWAKING) { signal (node.prev); }
		}
		
		if (interrupted) {
			if(interruptible) { r = -1; }
			else Thread.currentThread().interrupt(); /* r is '0' */
		}
		return r;
	}
}

