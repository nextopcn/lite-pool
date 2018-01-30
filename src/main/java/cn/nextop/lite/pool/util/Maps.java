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

package cn.nextop.lite.pool.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @author Jingqi Xu
 */
@SuppressWarnings("unchecked")
public final class Maps {

	public static int getInitialCapacity(int size) {
		return getInitialCapacity(size, 0.75d);
	}

	public static int getInitialCapacity(int size, double factor) {
		return (int)(size / factor) + 1;
	}
	
	/**
	 * 
	 */
	public static <K, V> HashMap<K, V> newHashMap(int size) {
		return new HashMap<K, V>(getInitialCapacity(size));
	}
	
	public static <V> LongHashMap<V> newLongHashMap(int size) {
		return new LongHashMap<V>(getInitialCapacity(size));
	}
	
	public static <K, V> HashMap<K, V> newHashMap(Map<K, V> map) {
		return map == null ? new HashMap<K, V>(0) : new HashMap<>(map);
	}
	
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
		return new ConcurrentHashMap<K, V>();
	}

	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(final int size) {
		return new LinkedHashMap<K, V>(getInitialCapacity(size));
	}
	
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(final int size) {
		return new ConcurrentHashMap<K, V>(getInitialCapacity(size));
	}

	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> m, K k, V v) {
		final V r = m.putIfAbsent(k, v); return r != null ? r : v;
	}
}
