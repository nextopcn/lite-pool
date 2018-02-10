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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Baoyi Chen
 */
public class ObjectPoolTest extends BaseTest {

    @Test
    public void testLeak() throws InterruptedException {
        Pool<TestObject1> pool = create(2, 10, 5000, 15000, 0, 5000, 30000, () -> {
            TestObject1 t =new TestObject1();
            System.out.println("created object:" + t);
            return t;
        }, v -> {
            System.out.println("deleted object:" + v);
        });
        pool.start();
        ExecutorService s = Executors.newFixedThreadPool(50);
        int count = 100;
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger acc = new AtomicInteger();
        for (int i = 0; i < count; i++) {
            s.submit(() -> {
                TestObject1 to = pool.acquire();
                if (to == null) {
                    acc.incrementAndGet();
                }
                latch.countDown();
            });
        }
        latch.await();
        assertEquals(90, acc.get());
        s.shutdown();
        pool.stop();
    }

    @Test
    public void testNoShrink() {
        Pool<TestObject1> pool = create(4, 10, 500, 15000, 0, 1000, 30000, () -> {
            TestObject1 t =new TestObject1();
            System.out.println("created object:" + t);
            return t;
        }, v -> {
            System.out.println("deleted object:" + v);
        });
        pool.start();
        for (int i = 0; i < 1000; i++) {
            TestObject1 t = null;
            try {
                t = pool.acquire();
                if (t != null)
                    Thread.sleep(5);
            } catch (Throwable cause) {
                cause.printStackTrace();
            } finally {
                if (t != null)
                    pool.release(t);
            }
        }
        pool.stop();
    }

    @Test
    public void test() throws Exception {
        Pool<TestObject1> pool = create(2, 10, 2000, 4000, 0, 8000, 30000, () -> {
            TestObject1 t =new TestObject1();
            System.out.println("created object:" + t);
            return t;
        }, v -> {
            System.out.println("deleted object:" + v);
        });
        pool.start();
        ExecutorService s = Executors.newFixedThreadPool(50);
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        int count = 10;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            s.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    TestObject1 t = null;
                    try {
                        t = pool.acquire();
                        Thread.sleep(3);
                        if (t != null) {
                            success.incrementAndGet();
                        } else {
                            failed.incrementAndGet();
                        }
                    } catch (Throwable cause) {
                    } finally {
                        if (t != null)
                            pool.release(t);
                    }
                }
                latch.countDown();
            });
        }
        latch.await();
        System.out.println("sleep 20 seconds, success:" + success.get() + ", failed:" + failed.get());
        TimeUnit.SECONDS.sleep(16);
        CountDownLatch latch1 = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            s.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    TestObject1 t = null;
                    try {
                        t = pool.acquire();
                        if (t != null)
                            Thread.sleep(3);
                    } catch (Throwable cause) {
                    } finally {
                        if (t != null)
                            pool.release(t);
                    }
                }
                latch1.countDown();
            });
        }
        latch1.await();
        System.out.println("done");
        TimeUnit.SECONDS.sleep(16);
        s.shutdown();
        pool.stop();
    }
}