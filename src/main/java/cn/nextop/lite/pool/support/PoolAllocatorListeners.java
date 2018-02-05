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

package cn.nextop.lite.pool.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.nextop.lite.pool.support.PoolAllocator.Slot;

/**
 * @author Jingqi Xu
 */
public class PoolAllocatorListeners<T> implements PoolAllocatorListener<T> {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(PoolAllocatorListeners.class);
	
	//
	private final String name;
	private final List<PoolAllocatorListener<T>> listeners = new CopyOnWriteArrayList<>();
	
	/**
	 * 
	 */
	public PoolAllocatorListeners(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 */
	public boolean addListener(PoolAllocatorListener<T> listener) {
		return this.listeners.add(listener);
	}
	
	public boolean delListener(PoolAllocatorListener<T> listener) {
		return this.listeners.remove(listener);
	}
	
	/**
	 * 
	 */
	@Override
	public void onAcquire(Slot<T> v) {
		for(final PoolAllocatorListener<T> listener : this.listeners) {
			try {
				listener.onAcquire(v);
			} catch(Throwable tx) {
				LOGGER.error("[" + this.name + "]failed to notify: " + v, tx);
			}
		}
	}
	
	@Override
	public void onRelease(Slot<T> v) {
		for(final PoolAllocatorListener<T> listener : this.listeners) {
			try {
				listener.onRelease(v);
			} catch(Throwable tx) {
				LOGGER.error("[" + this.name + "]failed to notify: " + v, tx);
			}
		}
	}
	
	@Override
	public void onLeakage(Slot<T> v) {
		for(final PoolAllocatorListener<T> listener : this.listeners) {
			try {
				listener.onLeakage(v);
			} catch(Throwable tx) {
				LOGGER.error("[" + this.name + "]failed to notify: " + v, tx);
			}
		}
	}
}
