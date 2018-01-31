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

import cn.nextop.lite.pool.glossary.Lifecycle;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Jingqi Xu
 * @param <T>
 */
public interface PoolAllocator<T> extends Lifecycle {
	
	/**
	 * 
	 */
	Slot<T> release(T t);
	
	Slot<T> acquire(long timeout, TimeUnit unit);
	
	/**
	 * 
	 */
	boolean addListener(PoolAllocatorListener<T> listener);
	
	boolean delListener(PoolAllocatorListener<T> listener);
	
	/**
	 * 
	 */
	interface Slot<T> {

		boolean isBusy(); boolean isIdle(); boolean isAlive(); boolean isValid();

		boolean isExpired(long now); boolean isRetired(long now); boolean isLeaked(long tenancy);

		T get(); long getId(); <V> V getCookie(Object key); Object setCookie(Object k, Object v);

		void touch(); boolean acquire(); boolean release(); boolean abandon(); boolean destroy();
	}
}
