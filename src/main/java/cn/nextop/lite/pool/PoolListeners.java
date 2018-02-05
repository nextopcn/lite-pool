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

package cn.nextop.lite.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Jingqi Xu
 */
public class PoolListeners<T> implements PoolListener<T> {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(PoolListeners.class);
	
	//
	private final String name;
	private final List<PoolListener<T>> listeners = new CopyOnWriteArrayList<>();
	
	/**
	 * 
	 */
	public PoolListeners(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 */
	public boolean addListener(PoolListener<T> listener) {
		return this.listeners.add(listener);
	}
	
	public boolean delListener(PoolListener<T> listener) {
		return this.listeners.remove(listener);
	}
	
	/**
	 * 
	 */
	@Override
	public void onEvent(PoolEvent<T> event) {
		for(final PoolListener<T> listener : this.listeners) {
			try {
				listener.onEvent(event);
			} catch(Throwable t) {
				LOGGER.error("[" + name + "]failed to notify: " + event, t);
			}
		}
	}
}
