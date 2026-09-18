/*
 * Copyright 2016-2018 Nextop Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.lite.pool.support.allocator;

import static cn.nextop.lite.pool.support.PoolAllocator.Phase.ACQUIRE;
import static cn.nextop.lite.pool.support.PoolAllocator.Phase.PULSE;
import static cn.nextop.lite.pool.support.PoolAllocator.Phase.RELEASE;
import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Status.BUSY;
import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Status.GONE;
import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Status.IDLE;
import static cn.nextop.lite.pool.util.Objects.requireNonNull;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.PoolConfig;
import cn.nextop.lite.pool.glossary.Lifecyclet;
import cn.nextop.lite.pool.glossary.Nullable;
import cn.nextop.lite.pool.glossary.Required;
import cn.nextop.lite.pool.support.PoolAllocator;
import cn.nextop.lite.pool.support.PoolAllocatorListener;
import cn.nextop.lite.pool.support.PoolAllocatorListeners;
import cn.nextop.lite.pool.util.Assertion;
import cn.nextop.lite.pool.util.Objects;
import cn.nextop.lite.pool.util.Strings;

/**
 * @author Baoyi Chen
 * @param <T>
 */
public abstract class AbstractAllocator<T> extends Lifecyclet implements PoolAllocator<T> {
	//
	protected final String name;
	protected final Pool<T> pool;
	protected final AtomicLong sequence;
	protected PoolAllocatorListeners<T> listeners;
	protected static final String PREFIX = "cn.nextop.lite.pool:type=PoolAllocator";

	//
	protected enum Status { BUSY, IDLE, GONE }
	protected abstract Slot<T> doRelease(T t);
	protected abstract Slot<T> doAcquire(long t, TimeUnit u);
	
	/**
	 *
	 */
	public AbstractAllocator( String name, Pool< T > pool ) {
		this.pool = pool; this.sequence = new AtomicLong(1L);
		listeners = new PoolAllocatorListeners<T>(this.name = name);
	}

	@Override
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		ObjectName n = new ObjectName(PREFIX + "(" + name + ")");
		MBeanServer m = ManagementFactory.getPlatformMBeanServer();
		if(m.isRegistered(n)) m.unregisterMBean(n); return timeout;
	}

	@Override
	protected void doStart() throws Exception {
		final var c = this.getConfig(); var s = c.getSupplier();
		Assertion.assertTrue(s != null, "supplier is required");
		
		final MBeanServer m = ManagementFactory.getPlatformMBeanServer();
		final ObjectName n = new ObjectName(PREFIX + "(" + this.name + ")");
		if(m.isRegistered(n)) m.unregisterMBean(n); m.registerMBean(this, n);
		
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		return Strings.build(this)
		.append("name" , name).toString();
	}
	
	protected Slot <T> wrap(T t) {
		var r = new SlotImpl(t); return r;
	}
	
	protected PoolConfig <T> getConfig() {
		return requireNonNull(this.pool).getConfig();
	}
	
	@Override
	public boolean addListener (PoolAllocatorListener<T> v) {
		final boolean r = listeners.addListener(v); return r;
	}
	
	@Override
	public boolean delListener (PoolAllocatorListener<T> v) {
		final boolean r = listeners.delListener(v); return r;
	}
	
	/**
	 * 
	 */
	@Override
	public Slot<T> release(@Required T t) {
		final var r = doRelease(requireNonNull(t));
		if (r != null) this.listeners.onRelease(r); return r;
	}
	
	@Override
	public Slot<T> acquire(long t , @Required TimeUnit u) {
		var r = doAcquire(t , u); if (r == null) return null;
		else { r.touch(); listeners.onAcquire(r); return r; }
	}
	
	/**
	 * 
	 */
	protected Slot<T> supply() {
		final var v = this.getConfig().getSupplier();
		var r = v.get(); return this.wrap(requireNonNull(r));
	}
	
	protected boolean consume(T item) {
		final var v = this.getConfig().getConsumer();
		if(v != null) { v.accept(item); } return (v != null);
	}
	
	protected boolean validate(Phase phase, T item) {
		final var v = this.getConfig().getValidator();
		if(v == null) return true; return v.test(phase, item);
	}
	
	/**
	 * 
	 */
	protected static class Identity<T> {
		public static <T> Identity<T> id(T t) { return new Identity<>(t); }
		protected final T t; public Identity(final T item) { this.t = item; }
		@Override public int hashCode() { return System.identityHashCode(t); }
		@Override public boolean equals(Object o) { return isEquals(this, o); }
	}
	
	protected static final boolean isTimeout(final long time, long timeout) {
		return (timeout > 0L && (time + timeout < System.currentTimeMillis()));
	}
	
	protected static final boolean isEquals(Identity<?> a , final Object b) {
		if(a == b) return true; else if(a == null || b == null) return false;
		return (b instanceof Identity x) ? a.t == x.t /** identity **/ : false;
	}
	
	protected static final boolean isEquals(Slot<?> a , @Nullable Object b) {
		if(a == b) return true; else if(a == null || b == null) return false;
		return (b instanceof Slot <?> x) ? a.getId().equals(x.getId()) : false;
	}
	
	/**
	 * 
	 */
	protected boolean isPulsable(@Nullable Slot<T> r) {
		if(r == null || !r.isAlive() || r.isExpired() || r.isRetired()) return false;
		return (getConfig().getValidation().isPulseEnabled()) ? r.isValid((PULSE)) : true;
	}
	
	protected boolean isAcquirable(@Nullable Slot<T> r) {
		if(r == null || !r.isAlive() || r.isExpired() || r.isRetired()) return false;
		return getConfig().getValidation().isAcquireEnabled() ? r.isValid(ACQUIRE) : true;
	}
	
	protected boolean isReleasable(@Nullable Slot<T> r) {
		if(r == null || !r.isAlive() || r.isExpired() || r.isRetired()) return false;
		return getConfig().getValidation().isReleaseEnabled() ? r.isValid(RELEASE) : true;
	}
	
	/**
	 * 
	 */
	protected class SlotImpl implements Slot<T> {
		//
		protected final T item; protected final Long id = sequence.getAndIncrement();
		protected final AtomicReference<Status> status = new AtomicReference<>(IDLE);
		protected final AtomicLong create = new AtomicLong(System.currentTimeMillis());
		protected final AtomicLong access = new AtomicLong(System.currentTimeMillis());
		protected final ConcurrentMap<Object, Object> cookies = new ConcurrentHashMap<>();
		
		//
		public SlotImpl(@Required T v) { this.item = v; }
		@Override public final T get() { return this.item; }
		@Override public final Long getId() { return this.id; }
		@Override public final int hashCode() { return Long.hashCode(this.id); }
		@Override public final String toString() { return Strings.buildEx(this); }
		@Override public final void touch() { access.set(System.currentTimeMillis()); }
		@Override public final boolean equals(Object rhs) { return isEquals(this, rhs); }
		@Override public <V> V getCookie(Object k) { return Objects.cast(cookies.get(k)); }
		@Override public Object setCookie(Object k , Object v) { return cookies.put(k , v); }
		@Override public final boolean isValid(Phase phase) { return validate(phase, item); }
		
		@Override public boolean acquire() { return this.status.compareAndSet(IDLE , BUSY); }
		@Override public boolean release() { return this.status.compareAndSet(BUSY , IDLE); }
		@Override public boolean abandon() { return this.status.compareAndSet(BUSY , GONE); }
		@Override public boolean destroy() { return this.status.compareAndSet(IDLE , GONE); }
		
		@Override public final boolean isBusy () { return this.status.get() == Status.BUSY; }
		@Override public final boolean isIdle () { return this.status.get() == Status.IDLE; }
		@Override public final boolean isAlive() { return this.status.get() != Status.GONE; }
		
		@Override public boolean isLeaked(long v) { return isBusy() && isTimeout(access.get(), v); }
		@Override public boolean isExpired() { return isTimeout(access.get(), getConfig().getTti()); }
		@Override public boolean isRetired() { return isTimeout(create.get(), getConfig().getTtl()); }
	}
}
