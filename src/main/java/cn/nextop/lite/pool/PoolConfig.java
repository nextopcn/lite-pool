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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import cn.nextop.lite.pool.glossary.Copyable;
import cn.nextop.lite.pool.util.Objects;

/**
 * 
 * @author Jingqi Xu
 */
public class PoolConfig<T> implements Copyable<PoolConfig<T>>, PoolConfigMXBean {
	//
	protected int minimum = 0;
	protected int maximum = 16;
	protected boolean fifo = false;
	protected Consumer<T> consumer;
	protected Supplier<T> supplier;
	protected BiPredicate<Phase, T> validator;
	protected long tti = TimeUnit.MINUTES.toMillis(15);
	protected long ttl = TimeUnit.MINUTES.toMillis(60);
	protected long tenancy = TimeUnit.MINUTES.toMillis(1);
	protected long timeout = TimeUnit.SECONDS.toMillis(5);
	protected long interval = TimeUnit.SECONDS.toMillis(60);
	protected PoolValidation validation = new PoolValidation((byte)1);/* PULSE */
	protected ConcurrentMap<Object , Object> cookies = new ConcurrentHashMap<>();
	
	/**
	 * 
	 */
	@Override
	public PoolConfig<T> copy() {
		PoolConfig<T> r = new PoolConfig <>();
		r.validation = this.validation.copy();
		r.minimum = this.minimum; r.maximum = this.maximum;
		r.tenancy = this.tenancy; r.interval = this.interval;
		r.tti = this.tti; r.ttl = this.ttl; r.fifo = this.fifo;
		r.timeout = this.timeout; r.validator = this.validator;
		r.supplier = this.supplier; r.consumer = this.consumer;
		r.cookies = new ConcurrentHashMap<>(cookies); return r;
	}
	
	/**
	 * 
	 */
	public long getTti() { return this.tti; }
	public long getTtl() { return this.ttl; }
	public void setTti(long v) { this.tti = v; }
	public void setTtl(long v) { this.ttl = v; }
	public boolean isFifo() { return this.fifo; }
	public int getMinimum() { return this.minimum; }
	public int getMaximum() { return this.maximum; }
	public long getTenancy() { return this.tenancy; }
	public long getTimeout() { return this.timeout; }
	public void setFifo(boolean v) { this.fifo = v; }
	public long getInterval() { return this.interval; }
	public void setMinimum(int v) { this.minimum = v; }
	public void setMaximum(int v) { this.maximum = v; }
	public void setTenancy(long v) { this.tenancy = v; }
	public void setTimeout(long v) { this.timeout = v; }
	public void setInterval(long v) { this.interval = v; }
	public Consumer<T> getConsumer() { return this.consumer; }
	public Supplier<T> getSupplier() { return this.supplier; }
	public PoolValidation getValidation() { return validation; }
	public void setConsumer(Consumer<T> v) { this.consumer = v; }
	public void setSupplier(Supplier<T> v) { this.supplier = v; }
	public void setValidation(PoolValidation v) { this.validation = v; }
	public BiPredicate<Phase, T> getValidator() { return this.validator; }
	public void setValidator(BiPredicate<Phase, T> v) { this.validator = v; }
	public <V> V getCookie(Object k) { return Objects.cast(this.cookies.get(k)); }
	public Object setCookie(Object k, Object v) { return this.cookies.put(k, v); }
}
