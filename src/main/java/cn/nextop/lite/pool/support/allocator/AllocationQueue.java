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

package cn.nextop.lite.pool.support.allocator;

import static cn.nextop.lite.pool.util.Objects.requireNonNull;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.glossary.Required;
import cn.nextop.lite.pool.latch.ProgressLatch64;
import cn.nextop.lite.pool.support.PoolAllocator.Slot;

/**
 * @author Baoyi Chen
 */
public class AllocationQueue<T> {
	//
	protected final Pool<T> pool;
	protected final ConcurrentSkipListMap< Long , Slot<T> > queue;
	protected final ProgressLatch64 latch = new ProgressLatch64();
	
	/**
	 * 
	 */
	public AllocationQueue(@Required Pool<T> v) {
		this.pool = requireNonNull(v); this.queue = new ConcurrentSkipListMap<>();
	}
	
	/**
	 * 
	 */
	public boolean offer (@Required Slot<T> slot) {
		var v = queue.putIfAbsent(slot.getId(), slot);
		if(v != null) { return false; } else { latch.getAndAdd(1L); return true; }
	}
	
	public boolean remove(@Required Slot<T> slot) {
		final var v = this.queue.remove(slot.getId());
		if(v == null) { return false; } else { latch.getAndAdd(1L); return true; }
	}
	
	/**
	 * 
	 */
	public Slot<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
		final var queue = this.queue; final var latch = this.latch;
		var ns = unit.toNanos(timeout); var n1 = (nanoTime() + ns);
		for(var progress = latch.get(); ; progress = latch.get()) {
			final Entry<Long , Slot<T>> e = queue.pollFirstEntry();
			if(e != null) return e.getValue(); var n2 = n1 - nanoTime();
			if(n2 <= 0L || !latch.await(progress , n2 , NANOSECONDS)) return null;
		}
	}
}
