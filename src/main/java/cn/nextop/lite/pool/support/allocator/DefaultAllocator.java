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

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.glossary.Lifecyclet;
import cn.nextop.lite.pool.support.PoolAllocator;
import cn.nextop.lite.pool.support.PoolAllocatorFactory;
import cn.nextop.lite.pool.support.allocator.allocation.AllocationQueue;
import cn.nextop.lite.pool.util.Concurrents;
import cn.nextop.lite.pool.util.DateTimes;
import cn.nextop.lite.pool.util.concurrent.executor.XExecutorService;
import cn.nextop.lite.pool.util.scheduler.impl.executor.ExecutorJob;
import cn.nextop.lite.pool.util.scheduler.impl.executor.ExecutorScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.nextop.lite.pool.PoolEvent.leakage;
import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Identity.id;
import static cn.nextop.lite.pool.util.Assertion.assertTrue;
import static cn.nextop.lite.pool.util.concurrent.executor.XExecutors.create;
import static cn.nextop.lite.pool.util.scheduler.impl.executor.ExecutorTrigger.fixDelay;
import static java.lang.Boolean.TRUE;
import static java.lang.System.nanoTime;

/**
 * 
 * @author Baoyi Chen
 * @param <T>
 */
public class DefaultAllocator<T> extends AbstractAllocator<T> {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAllocator.class);
	
	//
	protected static final String LEAKAGE = "$LEAKAGE";
	
	//
	protected AllocationQueue<T> queue;
	protected final XExecutorService executor;
	protected final ExecutorScheduler scheduler;
	protected final Map<Identity<T>, Slot<T>> slots;
	protected final AtomicInteger size = new AtomicInteger(0);
	protected final AtomicInteger idle = new AtomicInteger(0);
	protected final AtomicInteger wait = new AtomicInteger(0);

	/**
	 * 
	 */
	public DefaultAllocator(Pool<T> pool, String name) {
		super(pool, name); executor = create(name + ".executor", 1);
		slots = new ConcurrentHashMap<>(getConfig().getMaximum() * 4);
		start(scheduler = new ExecutorScheduler(name + ".scheduler", 1));
	}

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        queue = new AllocationQueue<>(getConfig().getMaximum(), policy);
        scheduler.schedule(new ExecutorJob(name + ".pulse", this::pulse,
        fixDelay(0L, getConfig().getInterval(), TimeUnit.MILLISECONDS)));
    }
	
	@Override
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		timeout = Lifecyclet.stopQuietly(this.scheduler, timeout, unit);
		timeout = Concurrents.terminateQuietly(this.executor, timeout, unit);
		return super.doStop(timeout, unit);
	}
	
	/**
	 *
	 */
	@Override
	protected Slot<T> doRelease(T item) {
		//
		final Identity<T> id = id(item);
		final Slot<T> r = slots.get(id); if(r == null) { return null; }
		
		//
		r.setCookie(LEAKAGE, Boolean.FALSE);
		if(isReleasable(r)) { if(r.release() && enqueue(r)) return r; }
		else if(r.abandon() && del(r)) { consume(r.get()); expand(1); }
		return null;
	}
	
	@Override
	protected Slot<T> doAcquire(long timeout, TimeUnit unit) {
		this.wait.incrementAndGet();
		try {
			expand(1);
			long n = nanoTime(), t = unit.toNanos(timeout), i = 200L;
			for ( ; t >= 0L; t -= (nanoTime() - n), n = nanoTime()) {
				final Slot<T> v = dequeue(Math.min(t, i), TimeUnit.NANOSECONDS);
				if (v == null) continue; /* timeout */
				else if (isAcquirable(v)) { if(v.acquire()) return v; }
				else if (v.destroy() && del(v)) { consume(v.get()); expand(1); }
			}
			return null;
		} catch (InterruptedException t) { Thread.currentThread().interrupt(); }
		finally { assertTrue((this.wait.decrementAndGet() >= 0)); } return null;
	}
	
	/**
	 *
	 */
	protected boolean isExpandable() {
		final int min = getConfig().getMinimum();
		final int max = getConfig().getMaximum();
		if (this.size.get() >= max) return false;
		return (this.idle.get() < min || this.wait.get() > this.size.get());
	}
	
	protected int shrink() {
		int r = 0;
		for (Slot<T> v : this.slots.values()) {
			if(isPulsable(v)) continue;
			if(v.destroy() && del(v)) { dequeue(v); consume(v.get()); r++; }
		}
		return r;
	}
	
	protected void expand(int n) {
		for(int i = 0; i < n; i++) {
			if (isExpandable()) this.executor.execute(() -> {
				try {
					if(!isExpandable()) return; final T t = supply();
					Slot<T> slot = new SlotImpl(t); add(slot); enqueue(slot);
				} catch (Throwable root) {
					LOGGER.error("[" + name + "]failed to expand pool", root);
				}
			});
		}
	}
	
	/**
	 *
	 */
	protected void pulse() {
		//
		final long mark = System.nanoTime();
		final boolean verbose = isVerbose();
		try {
			expand(shrink());
		} catch (Throwable root) {
			LOGGER.error("[" + name + "]failed to pulse pool", root);
		}
		
		// Leak?
		final long tenancy = getConfig().getTenancy();
		if(tenancy > 0) for(Slot<T> v : this.slots.values()) {
			if(!v.isLeaked(tenancy)) continue;
			if((TRUE == v.setCookie(LEAKAGE, TRUE))) continue;
			if(verbose) LOGGER.warn("[{}]leak, slot: {}", name, v);
			listeners.onLeakage(v); pool.publish(leakage(v.get()));
		}
		
		//
		if (!verbose) { return; } final long et = nanoTime() - mark;
		final int v1 = size.get(), v2 = idle.get(), v3 = wait.get();
		final Object[] args = new Object[] {this.name, v1, v2, v3, DateTimes.toMillis(et)};
		LOGGER.info("[{}]pulse, total: {}, idle: {}, wait: {}, elapsed time: {} ms", args);
	}
	
	/**
	 * 
	 */
	protected boolean add(final Slot<T> v) {
		boolean r = slots.put(id(v.get()), v) == null; if (r) size.incrementAndGet(); return r;
	}
	
	protected boolean del(final Slot<T> v) {
		boolean r = slots.remove(id(v.get())) == v; if(r) { size.decrementAndGet(); } return r;
	}
	
	protected boolean enqueue(Slot<T> slot) {
		final boolean r = this.queue.offer(slot); if (r) this.idle.incrementAndGet(); return r;
	}
	
	protected boolean dequeue(Slot<T> slot) {
		final boolean r = this.queue.remove(slot); if(r) this.idle.decrementAndGet(); return r;
	}
	
	protected Slot<T> dequeue(long t, TimeUnit unit) throws InterruptedException {
		final Slot<T> r = queue.poll(t, unit); if (r != null) idle.decrementAndGet(); return r;
	}
	
	/**
	 * 
	 */
	public static class Factory<T> implements PoolAllocatorFactory<T> {
		@Override public final PoolAllocator<T> create(final Pool<T> v) {
			String n = v.getName() + ".allocator.default"; return new DefaultAllocator<>(v, n);
		}
	}
}