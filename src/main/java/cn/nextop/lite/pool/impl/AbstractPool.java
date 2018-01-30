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

package cn.nextop.lite.pool.impl;

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.PoolConfig;
import cn.nextop.lite.pool.PoolEvent;
import cn.nextop.lite.pool.PoolListener;
import cn.nextop.lite.pool.PoolListeners;
import cn.nextop.lite.pool.glossary.Lifecyclet;
import cn.nextop.lite.pool.support.PoolAllocator;
import cn.nextop.lite.pool.support.PoolAllocatorFactory;
import cn.nextop.lite.pool.util.Strings;

import java.util.concurrent.TimeUnit;

import static cn.nextop.lite.pool.support.PoolAllocator.Slot;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Baoyi Chen
 */
public abstract class AbstractPool<T> extends Lifecyclet implements Pool<T> {
	//
	protected final String name;
	protected PoolAllocator<T> allocator;
	protected PoolListeners<T> listeners;
	protected PoolAllocatorFactory<T> factory;
	protected PoolConfig<T> config = new PoolConfig<>();
	
	/**
	 *
	 */
	protected AbstractPool(String name) {
		this.name = name;
		this.listeners = new PoolListeners<>(name + ".listeners");
	}
	
	@Override
	protected void doStart() throws Exception {
		Lifecyclet.start(this.allocator = this.factory.create(this));
	}
	
	@Override
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		return Lifecyclet.stopQuietly(this.allocator, timeout, unit);
	}
	
	/**
	 * 
	 */
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return Strings.build(this)
		.append("name", name).toString();
	}
	
	@Override
	public PoolConfig<T> getConfig() {
		return config;
	}
	
	public PoolListeners<T> getListeners() {
		return listeners;
	}
	
	public void setConfig(PoolConfig<T> config) {
		this.config = config;
	}
	
	public PoolAllocatorFactory<T> getFactory() {
		return factory;
	}
	
	public void setListeners(PoolListeners<T> v) {
		this.listeners = v;
	}
	
	public void setFactory(PoolAllocatorFactory<T> v) {
		factory = v;
	}
	
	/**
	 * Event
	 */
	@Override
	public void publish(PoolEvent<T> event) {
		this.listeners.onEvent(event);
	}
	
	@Override
	public boolean addListener(PoolListener<T> listener) {
		return this.listeners.addListener(listener);
	}
	
	@Override
	public boolean delListener(PoolListener<T> listener) {
		return this.listeners.delListener(listener);
	}
	
	/**
	 * 
	 */
	@Override
	public T acquire() {
		return acquire(config.getTimeout(), MILLISECONDS);
	}
	
	@Override
	public void release(T item) {
		final Slot<T> slot = this.allocator.release(item);
		if(slot != null) publish(PoolEvent.release(item));
	}
	
	@Override
	public T acquire(long timeout, TimeUnit unit) {
		Slot<T> r = allocator.acquire(timeout, unit); if(r == null) return null;
		final T item = (r.get()); publish(PoolEvent.acquire(item)); return item;
	}
}
