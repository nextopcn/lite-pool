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

package cn.nextop.lite.pool.glossary;

import cn.nextop.lite.pool.util.Concurrents;
import cn.nextop.lite.pool.util.Strings;
import cn.nextop.lite.pool.util.concurrent.RuntimeInterruptedException;
import cn.nextop.lite.pool.util.concurrent.future.impl.XFutureEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static cn.nextop.lite.pool.util.Iterators.iterable;

/**
 * @author Jingqi Xu
 */
public abstract class Lifecyclet implements Lifecycle {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(Lifecyclet.class);
	
	//
	private final XFutureEx started;
	protected volatile boolean verbose;
	private final AtomicInteger references;
	private final AtomicReference<Status> status;
	
	//
	private enum Status { STARTING, STARTED, STOPPING, STOPPED; }
	
	//
	protected abstract void doStart() throws Exception;
	protected abstract long doStop(long timeout, TimeUnit unit) throws Exception;
	
	/**
	 * 
	 */
	public Lifecyclet() {
		this.started = new XFutureEx();
		this.references = new AtomicInteger(0);
		this.status = new AtomicReference<>(Status.STOPPED);
	}
	
	/**
	 * 
	 */
	public boolean isVerbose() {
		return verbose;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	@Override
	public String toString() {
		return Strings.build(this).toString();
	}
	
	@Override
	public final boolean stop() {
		return stop(0L, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public final boolean isRunning() {
		return this.status.get() == Status.STARTED;
	}
	
	/**
	 * 
	 */
	@Override
	public final boolean start() {
		//
		this.references.getAndIncrement();
		if(!this.status.compareAndSet(Status.STOPPED, Status.STARTING)) {
			try {
				this.started.get(); return true;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); return false;
			} catch(ExecutionException e) {
				throw (RuntimeException)e.getCause(); // Cause is runtime
			}
		}
		
		//
		RuntimeException ex = null;
		try {
			doStart();
			LOGGER.info("lifecyclet: {} was successfully started", this);
			return true;
		} catch(Throwable e) {
			throw ex = translate(e);
		} finally {
			this.status.set(Status.STARTED);
			if(ex != null) this.started.complete(ex); else this.started.complete();
		}
	}
	
	@Override
	public final boolean stop(long timeout, TimeUnit unit)  {
		//
		if(this.references.decrementAndGet() != 0) return false;
		if(!this.status.compareAndSet(Status.STARTED, Status.STOPPING)) {
			return false;
		}
		
		//
		try {
			doStop(timeout, unit);
			LOGGER.info("lifecyclet: {} was successfully stopped", this);
			return true;
		} catch(Throwable e) { throw translate(e); } finally { this.status.set(Status.STOPPED); }
	}
	
	/**
	 * 
	 */
	protected final void assertRunning() throws IllegalStateException {
		if(!isRunning()) { throw new IllegalStateException(this + " is NOT running"); }
	}
	
	protected final void assertNotRunning() throws IllegalStateException {
		if(isRunning()) { throw new IllegalStateException(this + " should NOT be running"); }
	}
	
	/**
	 * 
	 */
	private final RuntimeException translate(final Throwable e) {
		if(e instanceof InterruptedException) { 
			Thread.currentThread().interrupt(); return new RuntimeInterruptedException(e);
		} else {
			return e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 */
	public static final long stop(Lifecycle t, long timeout, TimeUnit unit) {
		final long now = System.nanoTime();
		if(t != null && t.isRunning()) t.stop(timeout, unit);
		return Concurrents.sub(timeout, unit.convert(System.nanoTime() - now, TimeUnit.NANOSECONDS));
	}
	
	public static final long stopQuietly(Lifecycle t, long timeout, TimeUnit unit) {
		final long now = System.nanoTime();
		try { stop(t, timeout, unit); } 
		catch(Throwable e) { LOGGER.error("failed to stop lifecyclet: " + t, e); }
		return Concurrents.sub(timeout, unit.convert(System.nanoTime() - now, TimeUnit.NANOSECONDS));
	}
	
	/**
	 * 
	 */
	public static final <T extends Lifecycle> T start(final T t) { if(t != null) t.start(); return t; }
	
	public static final void stopQuietly(final Lifecycle t) { stopQuietly(t, 0L, TimeUnit.NANOSECONDS); }
	
	public static final void startQuietly(Lifecycle... ts) { if(ts != null) for(Lifecycle t : ts) startQuietly(t); }
	
	public static final void stopQuietly(Collection<? extends Lifecycle> ts) { for(Lifecycle t : iterable(ts)) stopQuietly(t); }
	
	public static final void startQuietly(Collection<? extends Lifecycle> ts) { for(Lifecycle t : iterable(ts)) startQuietly(t); }
	
	public static final <T extends Lifecycle> T startQuietly(T t) { try { if(t != null) t.start(); } catch(Throwable e) { LOGGER.error("failed to start: " + t, e); } return t; }
	
	public static final long stopQuietly(Collection<? extends Lifecycle> ts, long timeout, TimeUnit unit) { for(Lifecycle t : iterable(ts)) timeout = stopQuietly(t, timeout, unit); return timeout; }
}
