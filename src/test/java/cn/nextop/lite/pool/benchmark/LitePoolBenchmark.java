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

package cn.nextop.lite.pool.benchmark;

import cn.nextop.lite.pool.BaseTest;
import cn.nextop.lite.pool.Pool;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;

import java.util.concurrent.TimeUnit;

/**
 * @author Baoyi Chen
 */
@State(Scope.Benchmark)
public class LitePoolBenchmark extends BaseTest {

    public Pool<TestObject> pool;

    @Setup(Level.Trial)
    public void doSetup() {
        pool = createLitePool(10, 10, 5000, 15000, 0, 0, 30000, () -> new TestObject(), null);
        pool.getConfig().setLocal(false);
        pool.start();
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
        pool.stop();
    }

    @Benchmark
    @Threads(1)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void lite_pool_01_thread() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
    }

    @Benchmark
    @Threads(2)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void lite_pool_02_thread() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
    }

    @Benchmark
    @Threads(5)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void lite_pool_05_thread() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
    }

    @Benchmark
    @Threads(10)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void lite_pool_10_thread() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
    }

    @Benchmark
    @Threads(20)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void lite_pool_20_thread() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
    }

    @Benchmark
    @Threads(50)
    @CompilerControl(CompilerControl.Mode.INLINE)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void lite_pool_50_thread() {
        TestObject object = pool.acquire();
        if (object != null) pool.release(object);
    }
}
