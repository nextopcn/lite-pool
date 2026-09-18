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

import java.util.concurrent.TimeUnit;

import cn.nextop.lite.pool.glossary.Lifecycle;
import cn.nextop.lite.pool.glossary.Required;

/**
 * 
 * @author Jingqi Xu
 * @param <T>
 */
public interface PoolAllocator<T> extends Lifecycle, PoolAllocatorMXBean {

	/**
	 * 
	 */
	Slot<T> release(@Required T t);
	
	enum Phase { PULSE, ACQUIRE, RELEASE }
	
	Slot<T> acquire(long timeout, TimeUnit unit);
	
	boolean addListener(@Required PoolAllocatorListener<T> listener);
	
	boolean delListener(@Required PoolAllocatorListener<T> listener);
	
	/**
	 * 
	 */
	interface Slot<T> {
		
		boolean isBusy(); boolean isExpired(); /*** time to idle ***/
		
		boolean isIdle(); boolean isRetired(); /*** time to live ***/
		
		boolean isAlive(); boolean isLeaked(long tenancy); boolean isValid(@Required Phase phase);
		
		T get(); Long getId(); <V> V getCookie(Object key); Object setCookie(Object k , Object v);
		
		void touch (); boolean acquire(); boolean release(); boolean destroy(); boolean abandon();
	}
}
