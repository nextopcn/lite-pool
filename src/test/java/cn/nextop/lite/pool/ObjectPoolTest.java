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

import cn.nextop.lite.pool.impl.ObjectPool;
import cn.nextop.lite.pool.support.allocator.DefaultAllocator;
import cn.nextop.lite.pool.support.allocator.ThreadAllocator;
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
public class ObjectPoolTest {
    public static ObjectPool<TestObject> create(int minimum, int maximum, long timeout, long interval, long ttl,
                                                long tti) {
        ObjectPool<TestObject> pool = new ObjectPool<>("object.pool");
        pool.setVerbose(true);
        PoolConfig<TestObject> config = new PoolConfig<>();
        config.setConsumer(v -> {
            System.out.println("deleted object:" + v);
        });
        config.setSupplier(() -> {
            TestObject t =new TestObject();
            System.out.println("created object:" + t);
            return t;
        });
        config.setValidator(v -> true);
        config.setInterval(interval);
        config.setMinimum(minimum);
        config.setMaximum(maximum);
        config.setTimeout(timeout);
        config.setTtl(ttl);
        config.setTti(tti);
        pool.setConfig(config);
        pool.setFactory(new ThreadAllocator.Factory<>(new DefaultAllocator.Factory<>()));
        return pool;
    }

    @Test
    public void testLeak() throws InterruptedException {
        ObjectPool<TestObject> pool = create(2, 10, 5000, 15000, 0, 5000);
        pool.start();
        ExecutorService s = Executors.newFixedThreadPool(50);
        int count = 100;
        CountDownLatch latch = new CountDownLatch(count);
        AtomicInteger acc = new AtomicInteger();
        for (int i = 0; i < count; i++) {
            s.submit(new Runnable() {
                @Override public void run() {
                    TestObject to = pool.acquire();
                    if (to == null) {
                        acc.incrementAndGet();
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        assertEquals(90, acc.get());
        s.shutdown();
        pool.stop();
    }

    @Test
    public void testNoShrink() {
        ObjectPool<TestObject> pool = create(4, 10, 500, 15000, 0, 1000);
        pool.start();
        for (int i = 0; i < 1000; i++) {
            TestObject t = null;
            try {
                t = pool.acquire();
                if (t != null)
                    Thread.sleep(10);
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
        ObjectPool<TestObject> pool = create(2, 10, 2000, 5000, 0, 10000);
        pool.start();
        ExecutorService s = Executors.newFixedThreadPool(50);
        final AtomicInteger sucess = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        int count = 10;
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            s.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    TestObject t = null;
                    try {
                        t = pool.acquire();
                        Thread.sleep(10);
                        if (t != null) {
                            sucess.incrementAndGet();
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
        System.out.println("sleep 20 seconds, success:" + sucess.get() + ", failed:" + failed.get());
        TimeUnit.SECONDS.sleep(20);
        CountDownLatch latch1 = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            s.submit(() -> {
                for (int j = 0; j < 1000; j++) {
                    TestObject t = null;
                    try {
                        t = pool.acquire();
                        if (t != null)
                            Thread.sleep(10);
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
        TimeUnit.SECONDS.sleep(20);
        s.shutdown();
        pool.stop();
    }

    public static class TestObject {
        private static AtomicInteger acc = new AtomicInteger();
        private final int id;

        private TestObject() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.id = acc.getAndIncrement();
        }

        @Override
        public String toString() {
            return "TestObject:" + id;
        }
    }
}