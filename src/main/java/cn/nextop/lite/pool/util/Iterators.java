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

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Jingqi Xu
 */
@SuppressWarnings("unchecked")
public final class Iterators {
	//
	private static final Iterable<?> EMPTY = new IterableImpl<>(new IteratorImpl<>());

	/**
	 * 
	 */
	public static <T> Iterable<T> iterable(Iterator<T> iterator) {
		if(iterator == null) {
			return Objects.cast(EMPTY);
		} else {
			return new IterableImpl<T>(iterator);
		}
	}
	
	public static <T> Iterable<T> iterable(Collection<T> collection) {
		if(collection == null) {
			return Objects.cast(EMPTY);
		} else {
			return new IterableImpl<T>(collection.iterator());
		}
	}

	/**
	 *
	 */
	private static final class IteratorImpl<T> implements Iterator<T> {
		@Override public boolean hasNext() { return false; }
		@Override public T next() { return null; }
	}

	private static final class IterableImpl<T> implements Iterable<T> {
		private final Iterator<T> iterator;
		@Override public Iterator<T> iterator() { return this.iterator; }
		private IterableImpl(Iterator<T> iterator) { this.iterator = iterator; }
	}
}
