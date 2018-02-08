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

import cn.nextop.lite.pool.util.Objects;
import cn.nextop.lite.pool.util.Strings;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Jingqi Xu
 */
public class PoolConfig<T> implements PoolConfigMXBean {
	//
	protected boolean fifo = false;
	protected boolean local = true;
	protected Consumer<T> consumer;
	protected Supplier<T> supplier;
	protected Predicate<T> validator;
	protected volatile int minimum = 0;
	protected volatile int maximum = 16;
	protected long interval = TimeUnit.SECONDS.toMillis(15);
	protected volatile long tti = TimeUnit.MINUTES.toMillis(15);
	protected volatile long ttl = TimeUnit.MINUTES.toMillis(60);
	protected volatile long tenancy = TimeUnit.MINUTES.toMillis(1);
	protected volatile long timeout = TimeUnit.SECONDS.toMillis(8);
	protected PoolValidation validation = new PoolValidation((byte)1); // PULSE
	protected final ConcurrentMap<Object, Object> cookies = new ConcurrentHashMap<>();

	/**
	 *
	 */
	@Override public long getTti() { return this.tti; }
	@Override public long getTtl() { return this.ttl; }
	@Override public void setTti(long v) { this.tti = v; }
	@Override public void setTtl(long v) { this.ttl = v; }
	@Override public int getMinimum() { return this.minimum; }
	@Override public int getMaximum() { return this.maximum; }
	@Override public long getTenancy() { return this.tenancy; }
	@Override public long getTimeout() { return this.timeout; }
	@Override public void setMinimum(int v) { this.minimum = v; }
	@Override public void setMaximum(int v) { this.maximum = v; }
	@Override public void setTenancy(long v) { this.tenancy = v; }
	@Override public void setTimeout(long v) { this.timeout = v; }
	@Override public String toString() { return Strings.buildEx(this); }

	/**
	 *
	 */
	public boolean isFifo() { return this.fifo; }
	public boolean isLocal() { return this.local; }
	public void setFifo(boolean v) { this.fifo = v; }
	public void setLocal(boolean v) { this.local = v; }
	public long getInterval() { return this.interval; }
	public void setInterval(long v) { this.interval = v; }
	public Consumer<T> getConsumer() { return this.consumer; }
	public Supplier<T> getSupplier() { return this.supplier; }
	public PoolValidation getValidation() { return validation; }
	public Predicate<T> getValidator() { return this.validator; }
	public void setConsumer(Consumer<T> v) { this.consumer = v; }
	public void setSupplier(Supplier<T> v) { this.supplier = v; }
	public void setValidator(Predicate<T> v) { this.validator = v; }
	public void setValidation(PoolValidation v) { this.validation = v; }
	public <V> V getCookie(Object key) { return Objects.cast(this.cookies.get(key)); }
	public Object setCookie(Object key, Object value) { return cookies.put(key, value); }
}
