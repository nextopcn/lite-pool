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

package cn.nextop.lite.pool.support.allocator;

import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Identity.id;
import static cn.nextop.lite.pool.util.Assertion.assertTrue;
import static cn.nextop.lite.pool.util.Concurrents.terminateQuietly;
import static cn.nextop.lite.pool.util.Exceptions.getRootCause;
import static cn.nextop.lite.pool.util.Objects.requireNonNull;
import static cn.nextop.lite.pool.util.Strings.isEmpty;
import static java.lang.Boolean.TRUE;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.PoolConfig;
import cn.nextop.lite.pool.glossary.Required;
import cn.nextop.lite.pool.support.PoolAllocator;
import cn.nextop.lite.pool.support.PoolAllocatorFactory;
import cn.nextop.lite.pool.util.AssertionException;
import cn.nextop.lite.pool.util.concurrent.thread.XThreadFactory;

/**
 * 
 * @author Baoyi Chen
 * @param <T>
 */
public class DefaultAllocator<T> extends AbstractAllocator<T> {
	//
	protected final AllocationQueue<T> queue;
	protected final ScheduledExecutorService scheduler;
	protected final ConcurrentMap<Identity<T>, Slot<T>> slots;
	protected final AtomicInteger size = new AtomicInteger(0);
	protected final AtomicInteger idle = new AtomicInteger(0);
	protected final AtomicInteger wait = new AtomicInteger(0);
	
	//
	protected static final String LEAKAGE = "$LEAKAGE";/*** cookie ***/
	protected static Logger LOGGER = getLogger(DefaultAllocator.class);
	
	/**
	 *
	 */
	@Override
	public int getEntireCount () { return size.get(); }
	@Override
	public int getRestingCount() { return idle.get(); }
	@Override
	public int getPendingCount() { return wait.get(); }
	@Override
	public int getWorkingCount() { return size.get() - idle.get(); }
	
	/**
	 * 
	 */
	public DefaultAllocator(String name , Pool<T> pool) {
		super( name , pool ); this.queue = new AllocationQueue<>(pool);
		final XThreadFactory f = new XThreadFactory(this.name , false);
		this.slots = new ConcurrentHashMap<>(getConfig().getMaximum());
		this.scheduler = Executors.newSingleThreadScheduledExecutor(f);
	}
	
	@Override protected void doStart() throws Exception {
		super.doStart(); final PoolConfig<T> config = this.getConfig();
		var i = config.getInterval(); var unit = TimeUnit.MILLISECONDS;
		this.scheduler.scheduleWithFixedDelay(this::pulse, i, i, unit);
	}
	
	@Override
	protected long doStop(long t , final TimeUnit u) throws Exception {
		for(Slot<T> slot : this.slots.values()) { this.dispose(slot); }
		return super.doStop(terminateQuietly(this.scheduler, t, u), u);
	}
	
	/**
	 * 
	 */
	protected void pulse() {
		//
		final int min = getConfig().getMinimum();
		final long tenancy = getConfig().getTenancy();
		final boolean verbose = this.pool.isVerbose();
		try { shrink(); expand(min); } catch(Throwable cause) {
			LOGGER.warn("[{}]failed to pulse", this.name, cause);
		}
		
		// Leak?
		if (tenancy > 0) for (Slot <T> v : this.slots.values()) {
			if (!v.isLeaked(tenancy)) /*** nop ***/ { continue; }
			if (TRUE == v.setCookie(LEAKAGE, TRUE)) { continue; }
			LOGGER.warn("[{}]leak: {}", name, v); /** LEAKAGE **/
			listeners.onLeakage (v); /** @see PoolAllocatorListener **/
		}
		
		//
		int v1 = this.size.get(), max = getConfig().getMaximum();
		if(v1 > max) { LOGGER.warn("[{}]overload: {}", name, v1); }
		if(!verbose) return; int v2 = idle.get() , v3 = wait.get();
		final String p = "[{}]pulse, total: {}, idle: {}, wait: {}";
		var args = new Object[]{ name , v1, v2, v3 }; LOGGER.info(p , args);
	}
	
	/**
	 * 
	 */
	@Override
	protected Slot<T> doRelease(@Required T item) {
		Slot<T> r = this.slots.get(id(item)); if(r == null) { return null; }
		if (isReleasable(r)) { if(r.release()) { enqueue(r); return (r); } }
		if (retire(r, r::abandon)) { this.consume(r.get()); } return (null);
	}
	
	@Override
	protected Slot <T> doAcquire( long timeout , @Required TimeUnit unit ) {
		try {
			this.wait.incrementAndGet(); if (this.isExpandable()) expand(1);
			long now = System.nanoTime(); long nano = unit.toNanos(timeout);
			for(; nano >= 0; nano -= (nanoTime() - now), now = nanoTime()) {
				var r = dequeue(nano, NANOSECONDS); if (r == null) continue;
				if (this.isAcquirable(r)) { if (r.acquire()) { return r; } }
				if (retire(r , r::destroy)) { consume(r.get()); expand(1); }
			}
			return null;
		} catch(InterruptedException interrupted) {
			Thread.currentThread().interrupt(); /* cancelled */ return null;
		} finally {
			var waits = this.wait.decrementAndGet(); assertTrue(waits >= 0);
		}
	}
	
	/**
	 * 
	 */
	protected int shrink() {
		int n = 0; for(final var v : this.slots.values()) {
			try {
				if(!v.isIdle() || !this.dequeue(v)) /* idle */ { continue; }
				if(this.isPulsable(v)) /* valid */ { enqueue(v); continue; }
				if(this.retire(v, v::destroy)) { n += 1; consume(v.get()); }
			} catch(Throwable cause) {
				LOGGER.warn("[{}]failed to pulse: {}", this.name, v, cause);
				if(this.retire(v, v::destroy)) { n += 1; consume(v.get()); }
			}
		}
		return n;
	}
	
	protected void expand(int n) {
		for(; n > 0; n -= 1) this.scheduler.execute(() -> {
			try {
				if(!isRunning() || !isExpandable()) return;
				final var slot = supply(); if(add(slot)) this.enqueue(slot);
			} catch(Throwable error) {
				var cause = getRootCause(error); var m = cause.getMessage();
				if ((isEmpty(m) || (cause instanceof AssertionException))) {
					LOGGER.warn("[{}]failed to expand" , this.name , cause);
				} else {
					LOGGER.warn("[{}]failed to expand, cause: {}", name, m);
				}
			}
		});
	}
	
	/**
	 * 
	 */
	protected boolean isExpandable() {/*** @see also this.expand(int n) ***/
		var c = getConfig(); if (size.get() >= c.getMaximum()) return false;
		return this.idle.get() < Math.max(c.getMinimum() , this.wait.get());
	}
	
	protected boolean retire(@Required Slot<T> slot, BooleanSupplier test) {
		if(!test.getAsBoolean()) return false; final var r = this.del(slot);
		if(r) { LOGGER.info("[{}]retire: {}" , name , slot); return true ; }
		LOGGER.warn("[{}]failed to retire: {}", new Object[]{ name, slot });
		return false;
	}
	
	public static final class Factory<T> implements PoolAllocatorFactory<T> {
		@Override public final PoolAllocator<T> create (final Pool<T> pool) {
			return new DefaultAllocator<>(pool.getName() + ".allocator", pool);
		}
	}
	
	/**
	 * 
	 */
	protected boolean add(final @Required Slot<T> v) {
		requireNonNull(v); final var id = id(v.get());
		var r = slots.putIfAbsent(id, v) == null; if (r) this.size.incrementAndGet(); return r;
	}
	
	protected boolean del(final @Required Slot<T> v) {
		requireNonNull(v); final var id = id(v.get());
		var r = slots.remove(id, v); /* atomic */ if (r) this.size.decrementAndGet(); return r;
	}
	
	protected void dispose(final @Required Slot<T> v) {
		if (retire(v , v::destroy) || retire(v , v::abandon)) { dequeue(v); consume(v.get()); }
	}
	
	protected boolean enqueue(final @Required Slot<T> slot) {
		final boolean r = this.queue.offer (slot); if(r) this.idle.incrementAndGet(); return r;
	}
	
	protected boolean dequeue(final @Required Slot<T> slot) {
		final boolean r = this.queue.remove(slot); if(r) this.idle.decrementAndGet(); return r;
	}
	
	protected Slot<T> dequeue(long t, final @Required TimeUnit u) throws InterruptedException {
		Slot<T> r = this.queue.poll(t, u); if(r != null) this.idle.decrementAndGet(); return r;
	}
}
