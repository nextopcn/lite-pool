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

package cn.nextop.lite.pool.util;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

import static java.lang.Math.max;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 
 * @author Jingqi Xu
 */
public final class Concurrents {
	
	/**
	 * 
	 */
	public static long sub(long v1, long v2) {
		return max(max(v1, 0) - max(v2, 0), 0);
	}
	
	/**
	 * 
	 */
	public static final long initialDelay(long unit) {
		return initialDelay(unit, 0d); // Initial delay with 0 delta
	}
	
	public static final long initialDelay(long unit, double delta) {
		final long now = System.currentTimeMillis();
		return (now / unit + 1) * unit - now + (int) (delta * unit);
	}
	
	/**
	 * 
	 */
	public static void run(ReentrantLock lock, Runnable task) {
		lock.lock(); try { task.run(); } finally { lock.unlock(); }
	}
	
	public static <T> T get(ReentrantLock lock, Supplier<T> task) {
		lock.lock(); try { return task.get(); } finally { lock.unlock(); }
	}
	
	public static <T> T call(ReentrantLock lock, Callable<T> task)
	throws Exception {
		lock.lock(); try { return task.call(); } finally { lock.unlock(); }
	}
	
	public static <T> T read(StampedLock lock, Supplier<T> supplier) {
		//
		final long stamp1 = lock.tryOptimisticRead(); 
		final T r = supplier.get(); if(lock.validate(stamp1)) return r;
		
		// 
		final long stamp2 = lock.readLock();
		try { return supplier.get(); } finally { lock.unlockRead(stamp2); }
	}
	
	/**
	 * 
	 */
	public static void delayQuietly(Date date) {
		try {
			delay(date);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public static void delayQuietly(long timeout) {
		try {
			delay(timeout);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public static void delayQuietly(long timeout, TimeUnit unit) {
		try {
			delay(timeout, unit);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public static long delay(long timeout) throws InterruptedException {
		return delay(timeout, TimeUnit.MILLISECONDS);
	}
	
	public static long delay(final Date date) throws InterruptedException {
		return delay(date.getTime() - System.currentTimeMillis(), MILLISECONDS);
	}
	
	public static long delay(long timeout, TimeUnit unit) throws InterruptedException {
		// Precondition checking
		if(unit == null) throw new IllegalArgumentException("invalid parameter unit");
		if(timeout <= 0) return 0;
		
		//
		final long now = System.nanoTime();
		final Object object = new Object();
		synchronized(object) { unit.timedWait(object, timeout); }
		return sub(timeout, unit.convert(System.nanoTime() - now, TimeUnit.NANOSECONDS));
	}
	
	/**
	 * 
	 */
	public static long timedWait(Object obj, long timeout)
	throws InterruptedException {
		return timedWait(obj, timeout, TimeUnit.MILLISECONDS);
	}
	
	public static long timedWait(Object obj, long timeout, TimeUnit unit)
	throws InterruptedException {
		// Precondition checking
		if(obj == null) throw new IllegalArgumentException("invalid parameter obj");
		if(unit == null) throw new IllegalArgumentException("invalid parameter unit");
		if(timeout <= 0) return 0;
		
		//
		final long now = System.nanoTime();
		synchronized(obj) {
			unit.timedWait(obj, timeout);
		}
		final long elapsedTime = System.nanoTime() - now;
		return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
	}
	
	/**
	 * 
	 */
	public static long timedJoin(Thread thread, long timeout)
	throws InterruptedException {
		return timedJoin(thread, timeout, TimeUnit.MILLISECONDS);
	}
	
	public static long timedJoin(Thread thread, long timeout, TimeUnit unit)
	throws InterruptedException {
		// Precondition checking
		if(thread == null) return timeout;
		if(timeout <= 0) return 0;
		
		//
		final long now = System.nanoTime();
		unit.timedJoin(thread, timeout);
		final long elapsedTime = System.nanoTime() - now;
		return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
	}
	
	public static long timedJoinQuietly(Thread thread, long timeout, TimeUnit unit) {
		final long now = System.nanoTime();
		try {
			return timedJoin(thread, timeout, unit);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			final long elapsedTime = System.nanoTime() - now;
			return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
		}
	}
	
	/**
	 * 
	 */
	public static long terminate(ExecutorService exec, long timeout, TimeUnit unit)
	throws InterruptedException {
		// Precondition checking
		if(exec == null) return timeout;
		if(!exec.isShutdown()) exec.shutdown();
		
		//
		if(timeout <= 0) {
			return 0;
		} else {
			final long now = System.nanoTime();
			exec.awaitTermination(timeout, unit);
			final long elapsedTime = System.nanoTime() - now;
			return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
		}
	}
	
	public static long terminateQuietly(ExecutorService exec, long timeout, TimeUnit unit) {
		final long now = System.nanoTime();
		try {
			return terminate(exec, timeout, unit);
		} catch(InterruptedException e) {
			Thread.currentThread().interrupt();
			final long elapsedTime = System.nanoTime() - now;
			return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
		}
	}
	
	public static long terminateQuietly(ExecutorService[] execs, long timeout, TimeUnit unit) {
		if(execs == null || execs.length == 0) return timeout;
		for(ExecutorService exec : execs) timeout = terminateQuietly(exec, timeout, unit); return timeout;
	}
	
	public static long terminateQuietly(Collection<? extends ExecutorService> execs, long timeout, TimeUnit unit) {
		if(execs == null || execs.isEmpty()) return timeout;
		for(ExecutorService exec : execs) timeout = terminateQuietly(exec, timeout, unit); return timeout;
	}
}
