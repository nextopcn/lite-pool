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

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static cn.nextop.lite.pool.PoolValidation.RELEASE;
import static cn.nextop.lite.pool.PoolValidation.ACQUIRE;
import static cn.nextop.lite.pool.PoolValidation.PULSE;
import static org.junit.Assert.*;

/**
 * @author Baoyi Chen
 */
public class PoolValidationTest extends BaseTest {
    @Test
    public void test() throws InterruptedException {
        AtomicInteger acc = new AtomicInteger(0);
        AtomicInteger acc1 = new AtomicInteger(0);
        Pool<TestObject2> pool = create(2, 10, 3000, 5000, 0, 10000, 4000, () -> {
            TestObject2 o = new TestObject2();
            acc1.incrementAndGet();
            return o;
        }, v -> { if (!v.valid) acc.incrementAndGet(); });
        pool.getConfig().setValidation(new PoolValidation((byte)(RELEASE | ACQUIRE | PULSE)));
        pool.getConfig().setValidator(v -> v.valid);
        pool.start();
        Thread.sleep(500);
        assertEquals(2, acc1.get());
        TestObject2 o = pool.acquire();
        if (o != null) {
            o.valid = false;
            pool.release(o);
        }
        System.out.println("released");
        Thread.sleep(500);
        assertEquals(3, acc1.get());
        pool.stop();
        assertEquals(1, acc.get());
    }

    @Test
    public void test1() throws InterruptedException {
        Pool<TestObject2> pool = create(2, 10, 1000, 5000, 0, 10000, 4000, () -> {
            TestObject2 o = new TestObject2();
            o.valid = false;
            return o;
        }, null);
        pool.getConfig().setValidation(new PoolValidation((byte)(RELEASE | ACQUIRE | PULSE)));
        pool.getConfig().setValidator(v -> v.valid);
        pool.start();
        Thread.sleep(500);
        TestObject2 o = pool.acquire();
        assertNull(o);
        Thread.sleep(500);
        pool.stop();
    }

}