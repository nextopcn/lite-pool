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

package cn.nextop.lite.pool.support.allocator.allocation;

import cn.nextop.lite.pool.support.PoolAllocator.Slot;
import cn.nextop.lite.pool.util.Assertion;
import cn.nextop.lite.pool.util.Comparators;
import cn.nextop.lite.pool.util.LongHashMap;
import cn.nextop.lite.pool.util.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static cn.nextop.lite.pool.util.Assertion.assertTrue;
import static cn.nextop.lite.pool.util.Comparators.cmp;

/**
 * @author Baoyi Chen
 */
public class AllocationQueue<E> {
	//
	protected final ReentrantLock lock;
	protected final List<Entry<E>> values;
	protected final Condition notFull, notEmpty;
	protected final LongHashMap<Entry<E>> index;
	protected final Comparator<Entry<E>> comparator;
	protected final AtomicLong sequence = new AtomicLong(0L);
	
	/**
	 * 
	 */
	public AllocationQueue(AllocationPolicy policy) {
		this(16, policy);
	}
	
	public AllocationQueue(int initial, AllocationPolicy policy) {
		this(initial, false, policy);
	}
	
	public AllocationQueue(int initial, boolean fair, AllocationPolicy policy) {
		this.lock = new ReentrantLock(fair);
		this.comparator = new PolicyComparator(policy);
		this.notFull = lock.newCondition(); this.notEmpty = lock.newCondition();
		values = new ArrayList<>(initial); index = Maps.newLongHashMap(initial);
	}
	
	/**
	 * 
	 */
	public boolean remove(final Slot<E> slot) {
		Objects.requireNonNull(slot);
		final ReentrantLock lock = this.lock; lock.lock();
		try {
			//
			Entry<E> v = index.remove(slot.getId()); if((v == null)) return false;
			int index = Collections.binarySearch(this.values, v, this.comparator);
			
			//
			assertTrue((index >= 0)); this.values.remove(index); notFull.signal();
			Assertion.assertTrue(this.index.size() == values.size()); return true;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 
	 */
	public boolean offer(final Slot<E> slot) {
		Objects.requireNonNull(slot);
		final ReentrantLock lock = this.lock; lock.lock();
		try {
			// Unbounded
			if(this.index.get(slot.getId()) != null) return false;
			final Entry<E> next = new Entry<>(slot, this.sequence.incrementAndGet());
			int index = Collections.binarySearch(this.values, next, this.comparator);
			
			//
			Assertion.assertTrue(index < 0); index = (-index - 1);
			this.index.put(slot.getId(), next); this.values.add(index, next); // sort
			notEmpty.signal(); assertTrue((this.index.size() == this.values.size()));
			return true;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 
	 */
	public Slot<E> poll (long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock; lock.lockInterruptibly();
		try {
			//
			while (this.values.size() == 0) {
				if(nanos < 0) return null; nanos = this.notEmpty.awaitNanos(nanos);
			}
			
			//
			Entry<E> v = this.values.remove(0); /* first */ index.remove(v.slot.getId());
			notFull.signal(); assertTrue((index.size() == values.size())); return v.slot;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * 
	 */
	public int size() {
		final ReentrantLock lock = this.lock; lock.lock();
		final int r; try { r = this.values.size(); } finally { lock.unlock(); } return r;
	}
	
	/**
	 * 
	 */
	protected static class Entry<E> {
		protected final Slot<E> slot; protected final long sequence;
		public Entry(final Slot<E> v, final long w) { this.slot = v; this.sequence = w; }
	}
	
	/**
	 * 
	 */
	protected class PolicyComparator implements Comparator<Entry<E>> {
		//
		protected final AllocationPolicy policy;
		protected PolicyComparator(AllocationPolicy v) { policy = v; }
		
		//
		@Override public int compare(Entry<E> v1, Entry<E> v2) {
			final boolean asc = (this.policy == AllocationPolicy.FIFO);
			int r = Comparators.cmp(v1.sequence, v2.sequence, asc); if((r != 0)) return r;
			return cmp(v1.slot.getId(), v2.slot.getId(), asc); /* should not reach here */
		}
	}
}
