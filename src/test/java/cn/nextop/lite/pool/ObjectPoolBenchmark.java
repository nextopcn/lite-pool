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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Baoyi Chen
 */
@State(Scope.Benchmark)
public class ObjectPoolBenchmark {

    public ObjectPool<TestObject> pool;

    @Setup(Level.Trial)
    public void doSetup() {
        pool = create(2, 10, 5000, 15000, 0, 0);
        pool.start();
    }
    @TearDown(Level.Trial)
    public void doTearDown() {
        pool.stop();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void test() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
        else
            System.out.println("error");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(ObjectPoolBenchmark.class.getSimpleName())
            .warmupIterations(8)
            .measurementIterations(8)
            .forks(1)
            .threads(1)
            .build();

        new Runner(opt).run();
    }

    public static ObjectPool<TestObject> create(int minimum, int maximum, long timeout, long interval, long ttl, long tti) {
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

    public static class TestObject {
        private static AtomicInteger acc = new AtomicInteger();
        private final int id;

        private TestObject() {
            this.id = acc.getAndIncrement();
        }

        @Override
        public String toString() {
            return "TestObject:" + id;
        }
    }
}
