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

/**
 * @author Baoyi Chen
 */
@State(Scope.Benchmark)
public class ObjectPoolBenchmark {

    public Pool<TestObject> pool;

    @Setup(Level.Trial)
    public void doSetup() {
        pool = create(10, 10, 5000, 15000, 0, 0);
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
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(ObjectPoolBenchmark.class.getSimpleName())
            .warmupIterations(10)
            .measurementIterations(10)
            .forks(1)
            .threads(1)
            .build();

        new Runner(opt).run();
    }

    public static Pool<TestObject> create(int minimum, int maximum, long timeout, long interval, long ttl, long tti) {
        PoolBuilder<TestObject> builder = new PoolBuilder<>();
        Pool<TestObject> pool = builder.local(true).supplier(() -> new TestObject()).
                interval(interval).minimum(minimum).
                maximum(maximum).timeout(timeout).
                ttl(ttl).tti(tti).verbose(false).build("object pool");
        return pool;
    }

    public static class TestObject {
    }
}
