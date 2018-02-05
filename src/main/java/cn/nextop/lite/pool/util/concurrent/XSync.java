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

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author Jingqi Xu
 */
public final class XSync extends AbstractQueuedSynchronizer {
	//
	private static final long serialVersionUID = -1239566708710683761L;
	
	/**
	 * 
	 */
	public XSync(int count) {
		setState(count);
	}
	
	/**
	 * 
	 */
	public int getCount() {
		return getState();
	}
	
	@Override
	public int tryAcquireShared(int acquires) {
		return (getState() == 0) ? 1 : -1;
	}
	
	@Override
	public boolean tryReleaseShared(int releases) {
		for (;;) {
			final int c = getState(); if (c == 0) return false;
			int nc = c - 1; if (compareAndSetState(c, nc)) return nc == 0;
		}
	}
}
