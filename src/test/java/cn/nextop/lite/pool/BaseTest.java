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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Baoyi Chen
 */
public abstract class BaseTest {

    public <T> Pool<T> create(int minimum, int maximum, long timeout, long interval, long ttl, long tti, long tenancy, Supplier<T> supplier, Consumer<T> consumer) {
        PoolBuilder<T> builder = new PoolBuilder<>();
        Pool<T> pool = builder.local(true).supplier(supplier).consumer(consumer).
                interval(interval).minimum(minimum).
                maximum(maximum).timeout(timeout).tenancy(tenancy).
                ttl(ttl).tti(tti).verbose(false).build("object pool");
        return pool;
    }

    public static class TestObject {
    }

    public static class TestObject1 {
        private static AtomicInteger acc = new AtomicInteger();
        private final int id;

        protected TestObject1() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.id = acc.getAndIncrement();
        }

        @Override
        public String toString() {
            return "TestObject1:" + id;
        }
    }

    public static class TestObject2 {
        public volatile boolean valid = true;
    }
}
