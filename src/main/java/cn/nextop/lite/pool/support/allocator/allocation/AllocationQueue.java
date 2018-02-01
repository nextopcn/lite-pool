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

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.support.PoolAllocator.Slot;
import cn.nextop.lite.pool.util.Assertion;
import cn.nextop.lite.pool.util.Comparators;
import cn.nextop.lite.pool.util.LongHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.nextop.lite.pool.util.Assertion.assertTrue;
import static cn.nextop.lite.pool.util.Comparators.cmp;

/**
 * @author Baoyi Chen
 */
public class AllocationQueue<T> {
	//
	protected long sequence = 0L;
	protected final Pool<T> pool;
	protected final boolean fifo;
	protected final ReadWriteLock lock;
	protected final List<Entry<T>> values;
	protected final Condition notFull, notEmpty;
	protected final LongHashMap<Entry<T>> index;
	protected final Comparator<Entry<T>> comparator = new FifoComparator();

	/**
	 *
	 */
	public AllocationQueue(Pool<T> pool) {
		this(pool, false);
	}

	public AllocationQueue(Pool<T> pool, boolean fair) {
		//
		this.pool = pool; this.fifo = pool.getConfig().isFifo();
		final int v = Math.min(Math.max(pool.getConfig().getMaximum(), 32), 256);
		this.values = new ArrayList<>(v); this.index = new LongHashMap<>(v << 1);

		//
		lock = new ReentrantReadWriteLock(fair); Lock write = lock.writeLock();
		this.notFull = write.newCondition(); this.notEmpty = write.newCondition();
	}

	/**
	 *
	 */
	public boolean remove(final Slot<T> slot) {
		Objects.requireNonNull(slot);
		final Lock lock = this.lock.writeLock(); lock.lock();
		try {
			//
			Entry<T> v = index.remove(slot.getId()); if((v == null)) return false;
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
	public boolean offer(final Slot<T> slot) {
		//
		Objects.requireNonNull(slot);

		// Fast path to remove duplication for thread allocator
		final boolean thread = this.pool.getConfig().isLocal();
		if(thread && exists(slot)) return false; // ReadLock, @see ThreadAllocator

		//
		final Lock lock = this.lock.writeLock(); lock.lock();
		try {
			//
			long id = slot.getId(); if((this.index.containsKey(id))) return false;
			final long seq = ++this.sequence; Entry<T> n = new Entry<>(slot, seq);

			//
			if((fifo)) values.add(0, n); /* head */ else values.add(n); /* tail */
			this.index.put(id, n); /* unique, unbounded */ this.notEmpty.signal();
			Assertion.assertTrue(this.index.size() == values.size()); return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 *
	 */
	public Slot<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final Lock lock = this.lock.writeLock(); lock.lockInterruptibly();
		try {
			//
			while (this.values.size() == 0) {
				if (nanos < 0) return null; nanos = this.notEmpty.awaitNanos(nanos);
			}

			//
			final Entry<T> v = this.values.remove((this.values.size() - 1)); // tail
			final long id = v.slot.getId(); this.index.remove(id); notFull.signal();
			Assertion.assertTrue(this.index.size() == values.size()); return v.slot;
		} finally {
			lock.unlock();
		}
	}

	/**
	 *
	 */
	public int size() {
		final Lock lock = this.lock.readLock(); lock.lock();
		try { return this.values.size(); } finally { lock.unlock(); }
	}

	public boolean exists(Slot<T> slot) {
		final Lock lock = this.lock.readLock(); lock.lock();
		try { return (this.index.containsKey(slot.getId())); } finally { lock.unlock(); }
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
	protected class FifoComparator implements Comparator<Entry<T>> {
		@Override public final int compare(Entry<T> v1, Entry<T> v2) {
			boolean asc = !fifo; int r = cmp(v1.sequence, v2.sequence, asc); if((r != 0)) return r;
			return Comparators.cmp(v1.slot.getId(), v2.slot.getId(), asc); // should not reach here
		}
	}
}
