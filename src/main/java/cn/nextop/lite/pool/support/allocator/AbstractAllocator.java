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

import cn.nextop.lite.pool.Pool;
import cn.nextop.lite.pool.PoolConfig;
import cn.nextop.lite.pool.PoolValidation;
import cn.nextop.lite.pool.glossary.Lifecyclet;
import cn.nextop.lite.pool.support.PoolAllocator;
import cn.nextop.lite.pool.support.PoolAllocatorListener;
import cn.nextop.lite.pool.support.PoolAllocatorListeners;
import cn.nextop.lite.pool.util.Objects;
import cn.nextop.lite.pool.util.Strings;
import cn.nextop.lite.pool.util.concurrent.PaddedAtomicLong;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Status.BUSY;
import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Status.GONE;
import static cn.nextop.lite.pool.support.allocator.AbstractAllocator.Status.IDLE;

/**
 * 
 * @author Baoyi Chen
 * @param <T>
 */
public abstract class AbstractAllocator<T> extends Lifecyclet implements PoolAllocator<T> {
    //
    public enum Status { BUSY, IDLE, GONE };

    //
    protected final String name;
    protected final Pool<T> pool;
    protected final PaddedAtomicLong sequence;
    protected PoolAllocatorListeners<T> listeners;

    //
    protected abstract Slot<T> doRelease(T t);
    protected abstract Slot<T> doAcquire(long timeout, TimeUnit unit);

    /**
     *
     */
    public AbstractAllocator(final Pool<T> pool, String name) {
        this.verbose = pool.isVerbose(); this.name = name;
        this.pool = pool; this.sequence = new PaddedAtomicLong(1L);
        listeners = new PoolAllocatorListeners<>(name + ".listeners");
    }

    @Override
    protected void doStart() throws Exception {
        // NOP
    }

    @Override
    protected long doStop(long timeout, TimeUnit unit) throws Exception {
        return timeout;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return Strings.build(this)
                .append("name", name).toString();
    }

    protected PoolConfig<T> getConfig() {
        return this.pool.getConfig();
    }

    protected PoolValidation getValidation() {
        return this.pool.getConfig().getValidation();
    }

    /**
     *
     */
    @Override
    public Slot<T> release(T t) {
        final Slot<T> r = doRelease(t);
        if(r != null) this.listeners.onRelease(r); return r;
    }

    @Override
    public Slot<T> acquire(long timeout, TimeUnit unit) {
        final Slot<T> r = doAcquire(timeout, unit);
        if(r != null) this.listeners.onAcquire(r); return r;
    }

    /**
     *
     */
    @Override
    public boolean addListener(PoolAllocatorListener<T> listener) {
        return this.listeners.addListener(listener);
    }

    @Override
    public boolean delListener(PoolAllocatorListener<T> listener) {
        return this.listeners.delListener(listener);
    }

    /**
     *
     */
    protected T supply() {
        final Supplier<T> v = pool.getConfig().getSupplier(); return v.get();
    }

    protected boolean consume(final T item) {
        final Consumer<T> consumer = pool.getConfig().getConsumer();
        if (consumer != null) consumer.accept(item); return consumer != null;
    }

    protected boolean validate(final T item) {
        final Predicate<T> validator = pool.getConfig().getValidator();
        if (validator == null) return true; else return validator.test(item);
    }

    /**
     *
     */
    protected static final boolean isTimeout(final long time, long timeout) {
        return ((timeout > 0) && (time + timeout < System.currentTimeMillis()));
    }

    protected static final boolean isEquals(Identity<?> a, final Object b) {
        if(a == b) return true; else if(a == null || b == null) return false;
        return b instanceof Identity ? a.item == ((Identity<?>) b).item : false;
    }

    protected static final boolean isEquals(final Slot<?> a, final Object b) {
        if(a == b) return true; else if (a == null || b == null) return false;
        return b instanceof Slot<?> ? a.getId() == ((Slot<?>)b).getId() : false;
    }

    /**
     *
     */
    protected boolean isPulsable(final Slot<T> r) {
        if(r == null || !r.isAlive() || r.isExpired() || r.isRetired()) return false;
        return ((getConfig().getValidation().isPulseEnabled()) ? r.isValid() : true);
    }

    protected boolean isAcquirable(final Slot<T> r) {
        if(r == null || !r.isAlive() || r.isExpired() || r.isRetired()) return false;
        return (getConfig().getValidation().isAcquireEnabled() ? r.isValid() : true);
    }

    protected boolean isReleasable(final Slot<T> r) {
        if(r == null || !r.isAlive() || r.isExpired() || r.isRetired()) return false;
        return (getConfig().getValidation().isReleaseEnabled() ? r.isValid() : true);
    }

    /**
     *
     */
    protected static class Identity<T> {
        public static <T> Identity<T> id(T item) { return new Identity<>(item); }
        @Override public int hashCode() { return System.identityHashCode(item); }
        @Override public boolean equals(Object rhs) { return isEquals(this, rhs); }
        protected final T item; public Identity(final T item) { this.item = item; }
    }

    /**
     *
     */
    protected class SlotImpl implements Slot<T> {
        //
        protected final T item;
        protected final long id = sequence.getAndIncrement();
        protected final AtomicReference<Status> status = new AtomicReference<>(IDLE);
        protected volatile long create = System.currentTimeMillis(), access = this.create;
        protected final ConcurrentMap<Object, Object> cookies = new ConcurrentHashMap<>();

        //
        public SlotImpl (T item) { this.item = item; }
        @Override public T get() { return this.item; }
        @Override public long getId() { return this.id; }
        @Override public int hashCode() { return Long.hashCode(this.id); }
        @Override public String toString() { return Strings.buildEx(this); }
        @Override public boolean equals(Object rhs) { return isEquals(this, rhs); }
        @Override public <V> V getCookie(Object k) { return Objects.cast(this.cookies.get(k)); }
        @Override public Object setCookie(Object k, Object v) { return this.cookies.put(k, v); }

        //
        @Override public boolean isValid() { return validate(this.item); }
        @Override public boolean isBusy () { return this.status.get() == Status.BUSY; }
        @Override public boolean isIdle () { return this.status.get() == Status.IDLE; }
        @Override public boolean isAlive() { return this.status.get() != Status.GONE; }
        @Override public boolean isLeaked(long v) { return isBusy() && isTimeout(access, v); }
        @Override public boolean isRetired() { return isTimeout(create, getConfig().getTtl()); }
        @Override public boolean isExpired() { return isTimeout(access, getConfig().getTti()); }

        //
        @Override public void touch() { this.access = System.currentTimeMillis(); }
        @Override public boolean acquire() { boolean r = status.compareAndSet(IDLE, BUSY); if (r) touch(); return r; }
        @Override public boolean release() { boolean r = status.compareAndSet(BUSY, IDLE); if (r) touch(); return r; }
        @Override public boolean abandon() { boolean r = status.compareAndSet(BUSY, GONE); if (r) touch(); return r; }
        @Override public boolean destroy() { boolean r = status.compareAndSet(IDLE, GONE); if (r) touch(); return r; }
    }
}
