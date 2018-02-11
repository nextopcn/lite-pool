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
import cn.nextop.lite.pool.support.allocator.DefaultAllocator;
import cn.nextop.lite.pool.support.allocator.ThreadAllocator.Factory;
import cn.nextop.lite.pool.util.Strings;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import static cn.nextop.lite.pool.support.PoolAllocator.Slot;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
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
	protected static final String PREFIX = "cn.nextop.lite.pool:type=PoolConfig";

	/**
	 *
	 */
	protected AbstractPool(String name) {
		this.name = name;
		this.factory = new DefaultAllocator.Factory<T>();
		this.listeners = new PoolListeners<>(name + ".listeners");
	}

	@Override
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		final String n = PREFIX + "(" + this.name + ")";
		final MBeanServer mbean; final ObjectName object;
		mbean = getPlatformMBeanServer(); object = new ObjectName(n);
		if(mbean.isRegistered(object)) mbean.unregisterMBean(object);
		return Lifecyclet.stopQuietly(this.allocator, timeout, unit);
	}

	@Override
	protected void doStart() throws Exception {
		if (this.config.isLocal()) factory = new Factory<T>(factory);
		Lifecyclet.start(this.allocator = this.factory.create(this));
		final MBeanServer m = ManagementFactory.getPlatformMBeanServer();
		final ObjectName n = new ObjectName(PREFIX + "(" + this.name + ")");
		if(m.isRegistered(n)) m.unregisterMBean(n); m.registerMBean(config, n);
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
	public void notify(PoolEvent<T> event) {
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
		if (slot != null) notify(PoolEvent.release(item));
	}

	@Override
	public T acquire(long t, TimeUnit u) {
		Slot<T> r = allocator.acquire(t, u); if(r == null) return null;
		T item = r.get(); notify(PoolEvent.acquire(item)); return item;
	}
}
