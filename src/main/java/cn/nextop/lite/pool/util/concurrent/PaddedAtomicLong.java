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

package cn.nextop.lite.pool.util.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jingqi Xu
 */
public final class PaddedAtomicLong extends AtomicLong {
	//
	private static final long serialVersionUID = -4671483038892198626L;
	
	//
	public volatile long p1, p2, p3, p4, p5, p6 = 7L;
	public long sumPaddingToPreventOptimisation() { return p1 + p2 + p3 + p4 + p5 + p6; }
	
	/**
	 * 
	 */
	public PaddedAtomicLong() {
	}
	
	public PaddedAtomicLong(long value) {
		super(value);
	}
	
	/**
	 * 
	 */
	public final boolean setIfGt(final long next) {
		while (true) {
			long prev = get();
			if (next <= prev) {
				return false;
			} else {
				 if (compareAndSet(prev, next)) return true;
			}
		}
	}
	
	public final boolean setIfLt(long next) {
		while (true) {
			long prev = get();
			if (next >= prev) {
				return false;
			} else {
				 if (compareAndSet(prev, next)) return true;
			}
		}
	}
	
	public final boolean setIfGet(long next) {
		while (true) {
			long prev = get();
			if (next < prev) {
				return false;
			} else {
				 if (compareAndSet(prev, next)) return true;
			}
		}
	}
	
	public final boolean setIfLet(long next) {
		while (true) {
			long prev = get();
			if (next > prev) {
				return false;
			} else {
				 if (compareAndSet(prev, next)) return true;
			}
		}
	}
}
