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

package cn.nextop.lite.pool.util.concurrent.future.impl;


import cn.nextop.lite.pool.util.Strings;
import cn.nextop.lite.pool.util.concurrent.XSync;

import java.util.concurrent.Future;

/**
 * @author Jingqi Xu
 * @param <T>
 */
public abstract class AbstractFuture<T> implements Future<T> {
	//
	protected volatile boolean cancelled;
	protected final XSync sync = new XSync(1);
	
	/**
	 * 
	 */
	@Override
	public boolean isDone() {
		return sync.getCount() != 1;
	}
	
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}
	
	@Override
	public boolean cancel(boolean interrupt) {
		this.cancelled = true; return true;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return Strings.buildEx(this).toString();
	}
}
