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

package cn.nextop.lite.pool;

import static cn.nextop.lite.pool.support.PoolAllocator.Phase;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import cn.nextop.lite.pool.impl.ObjectPool;
import cn.nextop.lite.pool.support.PoolAllocatorFactory;
import cn.nextop.lite.pool.support.allocator.DefaultAllocator;

/**
 * @author Jingqi Xu
 */
public class PoolBuilder<T> {
	//
	protected boolean verbose = false;
	protected PoolConfig<T> config = new PoolConfig<>();
	protected PoolAllocatorFactory<T> factory = new DefaultAllocator.Factory<>();
	
	/**
	 * 
	 */
	public Pool<T> build(String name) {
		ObjectPool<T> r = new ObjectPool<>(name); r.setConfig(this.config);
		r.setFactory(this.factory); r.setVerbose(this.verbose); return (r);
	}
	
	/**
	 * 
	 */
	public PoolBuilder<T> tti (long ms) { config.setTti(ms); return this; }
	public PoolBuilder<T> ttl (long ms) { config.setTtl(ms); return this; }
	public PoolBuilder<T> fifo(boolean v) { config.setFifo(v); return this; }
	public PoolBuilder<T> minimum(int v) { config.setMinimum(v); return this; }
	public PoolBuilder<T> maximum(int v) { config.setMaximum(v); return this; }
	public PoolBuilder<T> verbose(boolean v) { this.verbose = v; return this; }
	public PoolBuilder<T> tenancy(long ms) { config.setTenancy(ms); return this; }
	public PoolBuilder<T> timeout(long ms) { config.setTimeout(ms); return this; }
	public PoolBuilder<T> interval(long ms) { config.setInterval(ms); return this; }
	public PoolBuilder<T> supplier(Supplier<T> v) { config.setSupplier(v); return this; }
	public PoolBuilder<T> consumer(Consumer<T> v) { config.setConsumer(v); return this; }
	public PoolBuilder<T> allocator(PoolAllocatorFactory<T> v) { factory = v; return this; }
	public PoolBuilder<T> validation(PoolValidation v) { config.setValidation(v); return this; }
	public PoolBuilder<T> validator(BiPredicate<Phase, T> v) { config.setValidator(v); return this; }
}
