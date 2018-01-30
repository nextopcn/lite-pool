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

package cn.nextop.lite.pool.util.concurrent.thread;


import cn.nextop.lite.pool.util.Objects;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * To take advantage of this thread-local, your thread must implement Aware.
 * By default, all threads created by XThreadFactory implement Aware.
 * All FastThreadLocal's instances must be declared in a static way.
 * 
 * @author Jingqi Xu
 */
public final class FastThreadLocal<T> {
	//
	private static final AtomicInteger INDEX = new AtomicInteger(0);
	
	//
	public interface Aware {
		
		<T> T getCookie();
		
		void setCookie(Object cookie);
	}
	
	//
	private final int index = INDEX.getAndIncrement();
	private final ThreadLocal<T> local = new ThreadLocal<>();
	
	/**
	 * 
	 */
	public FastThreadLocal(String name) {
	}
	
	/**
	 * 
	 */
	public T get() {
		final Thread t = Thread.currentThread();
		if(t instanceof Aware) {
			IntMap<T> map = ((Aware)t).getCookie();
			return map == null ? null : map.get(this);
		} else {
			return this.local.get();
		}
	}
	
	/**
	 * 
	 */
	public void set(T value) {
		final Thread t = Thread.currentThread();
		if(t instanceof Aware) {
			IntMap<T> map = ((Aware)t).getCookie();
			if(map == null) ((Aware)t).setCookie(map = new IntMap<>());
			map.put(this, value);
		} else {
			this.local.set(value);
		}
	}
	
	/**
	 * 
	 */
	private static final class IntMap<T> {
		//
		private Object[] table = new Object[8];
		
		/**
		 * 
		 */
		private void expand(final int index) {
			final Object[] t = this.table; this.table = Arrays.copyOf(t, index);
		}
		
		/**
		 * 
		 */
		private T get(FastThreadLocal<?> local) {
			final int i = local.index; final Object[] t = this.table; return i >= t.length ? null : Objects.cast(t[i]);
		}
		
		private T put(FastThreadLocal<?> local, T value) {
			int i = local.index; if(i >= table.length) expand(i); Object[] t = table; T r = Objects.cast(t[i]); t[i] = value; return r;
		}
	}
}
