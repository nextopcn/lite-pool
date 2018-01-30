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

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Jingqi Xu
 * @param <V>
 */
@SuppressWarnings({"unchecked"})
public class LongHashMap<V> {
	//
	protected static final int DEFAULT_CAPACITY = 16;
	protected static final int MAXIMUM_CAPACITY = 1 << 30;
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
	
	//
	protected int size;
	protected int threshold;
	protected Entry<V>[] table;
	
	/**
	 * 
	 */
	public LongHashMap() {
		this(DEFAULT_CAPACITY);
	}
	
	public LongHashMap(int capacity) {
		if(capacity < 0)
			throw new IllegalArgumentException("capacity: " + capacity);
		this.size = 0;
		this.table = new Entry[getCapacity(capacity)];
		this.threshold = (int)(this.table.length * DEFAULT_LOAD_FACTOR);
	}
	
	public LongHashMap(LongHashMap<V> rhs) {
		this.size = rhs.size;
		this.threshold = rhs.threshold;
		this.table = new Entry[rhs.table.length];
		for(int i = 0; i < this.table.length; i++) {
			this.table[i] = (rhs.table[i] == null ? null : new Entry<>(rhs.table[i]));
		}
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		Iterator<Map.Entry<Long, V>> it = entryIterator();
		if(!it.hasNext()) return "{}";
		final StringBuilder sb = new StringBuilder("{");
		while(true) {
			final Map.Entry<Long, V> e = it.next();
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue() == this ? "(this Map)" : e.getValue());
			if (!it.hasNext()) return sb.append('}').toString();
			sb.append(',').append(' ');
		}
	}
	
	/**
	 * 
	 */
	public int size() {
		return this.size;
	}
	
	public boolean isEmpty() {
		return this.size == 0;
	}
	
	public Set<Long> keySet() {
		return new KeySet();
	}
	
	public Collection<V> values() {
		return new Values();
	}
	
	public Set<Map.Entry<Long, V>> entrySet() {
		return new EntrySet();
	}
	
	public void clear() {
		if(this.size <= 0) return;
		Arrays.fill(this.table, null); this.size = 0;
	}
	
	public V get(long key) {
		final Entry<V> e = findEntry(key);
		return e == null ? null : e.value;
	}
	
	public V remove(long key) {
		final Entry<V> e = removeEntry(key);
		return e == null ? null : e.value;
	}
	
	public V put(long key, V value) {
		final Entry<V> e = findEntry(key);
		if(e != null) {
			 final V r = e.value;
			 e.value = value;
			 return r;
		} else {
			if(++this.size > this.threshold) rehash();
			createEntry(key, value);
			return null;
		}
	}
	
	public void putAll(LongHashMap<? extends V> m) {
		for(Map.Entry<Long, ? extends V> e : m.entrySet()) {
			 put(e.getKey(), e.getValue());
		}
	}
	
	public boolean containsKey(long key) {
		return findEntry(key) != null;
	}
	
	public boolean containsValue(Object value) {
		for(int i = this.table.length; --i >= 0;) {
			Entry<V> e = this.table[i];
			while (e != null) {
				if(value == e.value || value.equals(e.value)) return true;
				e = e.next;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	protected Iterator<V> valueIterator() {
		return new ValueIterator();
	}
	
	protected Iterator<Long> keyIterator() {
		return new KeyIterator();
	}
	
	protected Iterator<Map.Entry<Long, V>> entryIterator() {
		return new EntryIterator();
	}
	
	protected Entry<V> findEntry(long key) {
		final int index = indexFor(hash(key), this.table.length);
		for(Entry<V> e = this.table[index]; e != null; e = e.next) {
			if(key == e.key) return e;
		}
		return null;
	}
	
	protected Entry<V> createEntry(long key, V value) {
		final int index = indexFor(hash(key), this.table.length);
		final Entry<V> e = new Entry<V>(key, value);
		e.next = this.table[index];
		this.table[index] = e;
		return e;
	}
	
	protected Entry<V> removeEntry(long key) {
		//
		Entry<V> last = null;
		final int index = indexFor(hash(key), this.table.length);
		Entry<V> e = this.table[index];
		while (e != null && key != e.key) {
			last = e;
			e = e.next;
		}
		
		//
		if (e == null) {
			return null;
		} else if (last != null) {
			last.next = e.next;
		} else {
			this.table[index] = e.next;
		}
		this.size--;
		return e;
	}
	
	protected void rehash() {
		//
		final int length = (this.table.length << 1);
		final Entry<V>[] xtable = new Entry[length];
		for(int i = 0; i < this.table.length; i++) {
			for(Entry<V> e = this.table[i]; e != null; ) {
				final Entry<V> next = e.next;
				final int index = indexFor(hash(e.key), length);
				e.next = xtable[index];
				xtable[index] = e;
				e = next;
			}
		}
		
		//
		this.table = xtable;
		if(this.table.length >= MAXIMUM_CAPACITY) {
			this.threshold = Integer.MAX_VALUE;
		} else {
			this.threshold = (int)(this.table.length * DEFAULT_LOAD_FACTOR);
		}
	}
	
	/**
	 * 
	 */
	protected static int hash(long key) {
		return (int)(key ^ (key >>> 32)) & 0x7FFFFFFF;
	}
	
	protected static int indexFor(int hash, int length) {
		return hash & (length - 1);
	}
	
	protected static int getCapacity(int initialCapacity) {
		int r = 1; while(r < initialCapacity) r <<= 1;
		return r;
	}
	
	/**
	 * 
	 */
	private class Values extends AbstractCollection<V> {
		
		@Override
		public int size() {
			return size;
		}
		
		public void clear() {
			LongHashMap.this.clear();
		}
		
		@Override
		public Iterator<V> iterator() {
			return valueIterator();
		}
		
		public boolean contains(Object value) {
			return containsValue(value);
		}
	}
	
	private class KeySet extends AbstractSet<Long> {
		
		public int size() {
			return size;
		}
		
		public void clear() {
			LongHashMap.this.clear();
		}
		
		public Iterator<Long> iterator() {
			return keyIterator();
		}
		
		public boolean contains(Object key) {
			if(key == null || !(key instanceof Long)) return false;
			return containsKey((Long)key);
		}
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<Long, V>> {
		
		@Override
		public int size() {
			return size;
		}
		
		public void clear() {
			LongHashMap.this.clear();
		}
		
		public Iterator<Map.Entry<Long, V>> iterator() {
			return entryIterator();
		}
		
		public boolean contains(Object entry) {
			if (!(entry instanceof Map.Entry)) return false;
			final Map.Entry<Long, V> e = (Map.Entry<Long, V>)entry;
			final Entry<V> candidate = findEntry(e.getKey());
			return candidate != null && candidate.equals(e);
		}
	}
	
	/**
	 * Iterator
	 */
	private abstract class AbstractIterator {
		//
		protected int index = 0;
		protected Entry<V> next = prefetch();
		
		/**
		 * 
		 */
		public boolean hasNext() {
			return this.next != null;
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		protected Entry<V> prefetch() {
			if(this.next != null && this.next.next != null) {
				return this.next.next;
			}
			for(; this.index < table.length; this.index++) {
				if(table[this.index] != null) return table[this.index++];
			}
			return null;
		}
	}
	
	private class KeyIterator extends AbstractIterator implements Iterator<Long> {
		
		@Override
		public Long next() {
			if(this.next == null) return null;
			final Long r = this.next.key;
			this.next = prefetch();
			return r;
		}
	}
	
	private class ValueIterator extends AbstractIterator implements Iterator<V> {
		
		@Override
		public V next() {
			if(this.next == null) return null;
			final V r = this.next.value;
			this.next = prefetch();
			return r;
		}
	}
	
	private class EntryIterator extends AbstractIterator implements Iterator<Map.Entry<Long, V>> {
		
		@Override
		public Map.Entry<Long, V> next() {
			if(this.next == null) return null;
			Map.Entry<Long, V> r = this.next;
			this.next = prefetch();
			return r;
		}
	}
	
	/**
	 * 
	 */
	protected static class Entry<V> implements Map.Entry<Long, V> {
		//
		protected V value;
		protected final long key;
		protected Entry<V> next = null;
		
		/**
		 * 
		 */
		Entry(long key, V value) {
			this.key = key;
			this.value = value;
		}
		
		Entry(long key, V value, Entry<V> next) {
			this.key = key;
			this.next = next;
			this.value = value;
		}
		
		Entry(Entry<V> rhs) {
			this.key = rhs.key;
			this.value = rhs.value;
			for(Entry<V> x = this, e = rhs.next; e != null; e = e.next) {
				x.next = new Entry<>(e.key, e.value); x = x.next;
			}
		}
		
		/**
		 * 
		 */
		@Override
		public Long getKey() {
			return this.key;
		}
		
		@Override
		public V getValue() {
			return this.value;
		}
		
		@Override
		public V setValue(V value) {
			final V r = this.value;
			this.value = value;
			return r;
		}
		
		@Override
		public boolean equals(Object rhs) {
			if(rhs == null) return false;
			if(this == rhs) return true;
			if(rhs instanceof Entry) {
				final Entry<?> e = (Entry<?>) rhs;
				return (this.key == e.key) && (this.value == null ? e.value == null : this.value.equals(e.value));
			}
			return false;
		}
	}

}