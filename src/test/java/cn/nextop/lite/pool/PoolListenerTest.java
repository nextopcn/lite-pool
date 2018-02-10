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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Baoyi Chen
 */
public class PoolListenerTest extends BaseTest {

    @Test
    public void test() throws InterruptedException {
        Pool<TestObject> pool = create(2, 10, 3000, 5000, 0, 10000, 4000, () -> new TestObject(), null);
        AtomicInteger acquire = new AtomicInteger(0);
        AtomicInteger release = new AtomicInteger(0);
        AtomicInteger leakage = new AtomicInteger(0);
        pool.addListener(event -> {
            switch (event.getType()) {
                case ACQUIRE:
                    acquire.incrementAndGet();
                    break;
                case RELEASE:
                    release.incrementAndGet();
                    break;
                case LEAKAGE:
                    leakage.incrementAndGet();
                    break;
                default:
                    throw new AssertionError();
            }
        });

        pool.start();
        ExecutorService s = Executors.newFixedThreadPool(50);
        int count = 100;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            if (i < 10) {
                TestObject o = pool.acquire();
                if (o != null) {
                    System.out.println("release:" + o);
                    pool.release(o);
                }
                latch.countDown();
            } else {
                s.submit(() -> {
                    TestObject o = pool.acquire();
                    if (o != null) {
                        System.out.println("unrelease:" + o);
                    }
                    latch.countDown();
                });
            }

        }
        latch.await();
        s.shutdown();
        assertEquals(20, acquire.get());
        assertEquals(10, release.get());
        assertEquals(10, leakage.get());
        pool.stop();
    }
}