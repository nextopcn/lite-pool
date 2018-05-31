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

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * @author Jingqi Xu
 * @param <T>
 */
public class ThreadAllocator<T> extends AbstractAllocator<T> {
	//
	protected PoolAllocator<T> parent;
	protected final ThreadLocal<WeakReference<Slot<T>>> local;
	
	//
	public PoolAllocator<T> getParent() { return parent; }
	public void setParent (PoolAllocator<T> v) { parent = v; }

	/**
	 *
	 */
	@Override public int getEntireCount () { return parent.getEntireCount (); }
	@Override public int getRestingCount() { return parent.getRestingCount(); }
	@Override public int getWorkingCount() { return parent.getWorkingCount(); }
	@Override public int getPendingCount() { return parent.getPendingCount(); }

	/**
	 * 
	 */
	public ThreadAllocator(Pool<T> pool, String name) {
		super(pool, name); this.local = new ThreadLocal<>();
	}
	
	@Override
	protected void doStart() throws Exception {
		super.doStart(); Lifecyclet.start(this.parent);
	}
	
	@Override
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		return super.doStop(stopQuietly(parent, timeout, unit), unit);
	}
	
	/**
	 * 
	 */
	@Override
	protected Slot<T> doRelease(T t) {
		Slot<T> r = this.parent.release(t); if(r == null) return null;
		this.local.set(new WeakReference<>(r)); /* thread */ return r;
	}
	
	@Override
	protected Slot<T> doAcquire(long timeout, TimeUnit unit) {
		WeakReference<Slot<T>> guard = this.local.get();
		if(guard == null) this.local.set((guard = new WeakReference<>(null)));
		Slot<T> r = guard.get(); if (isAcquirable(r) && r.acquire()) return r;
		return parent.acquire(timeout, unit); // Delegate to parent to acquire
	}
	
	/**
	 * 
	 */
	public static class Factory<T> implements PoolAllocatorFactory<T> {
		//
		private PoolAllocatorFactory<T> parent;
		public Factory(final PoolAllocatorFactory<T> parent) { this.parent = parent; }
		
		//
		@Override public PoolAllocator<T> create(Pool<T> pool) {
			final ThreadAllocator<T> r; String n = pool.getName(); n += ".allocator.thread";
			r = new ThreadAllocator<T>(pool, n); r.setParent(parent.create(pool)); return r;
		}
	}
}
