package cn.nextop.lite.pool.benchmark;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import cn.nextop.lite.pool.PoolConfig;
import cn.nextop.lite.pool.benchmark.entity.PoolObject2;
import cn.nextop.lite.pool.impl.ObjectPool;
import cn.nextop.lite.pool.support.allocator.DefaultAllocator;

/**
 * 4 threads
 * Benchmark                     Mode  Cnt     Score     Error   Units
 * ObjectPoolBenchmark.commons  thrpt    5  2434.525 ± 168.744  ops/ms
 * ObjectPoolBenchmark.custom   thrpt    5  3843.456 ±  95.627  ops/ms
 * 
 * 1 thread
 * Benchmark                     Mode  Cnt     Score     Error   Units
 * ObjectPoolBenchmark.commons  thrpt    5  4889.806 ± 253.211  ops/ms
 * ObjectPoolBenchmark.custom   thrpt    5  6870.125 ± 594.868  ops/ms
 */
@Fork(1)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = SECONDS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(MILLISECONDS)
public class ObjectPoolBenchmark {
	
	private ObjectPool<PoolObject2> customPool;
	private GenericObjectPool<PoolObject2> commonsPool;
	
	@Setup(Level.Trial)
	public void doSetup() {
		customPool = createCustomPool(10, 10, 5000);
		commonsPool = createCommonsPool(10, 10, 5000);
	}
	
	@TearDown(Level.Trial)
	public void doTearDown() {
		customPool.stop();
		commonsPool.close();
	}
	
	@Benchmark
	public Object custom() {
		PoolObject2 o = customPool.acquire();
		customPool.release(o);
		return o;
	}
	
	@Benchmark
	public Object commons() throws Exception {
		PoolObject2 o = commonsPool.borrowObject();
		commonsPool.returnObject(o);
		return o;
	}
	
	private static ObjectPool<PoolObject2> createCustomPool(int min, int max, long timeout) {
		PoolConfig<PoolObject2> config = new PoolConfig<>();
		config.setMinimum(min);
		config.setMaximum(max);
		config.setTimeout(timeout);
		config.setInterval(60_000);
		config.setSupplier(PoolObject2::new);
		config.setConsumer(null);
		config.setValidator((v, w) -> true);
		
		ObjectPool<PoolObject2> pool = new ObjectPool<>("custom.pool");
		pool.setVerbose(false);
		pool.setConfig(config);
		pool.setFactory(new DefaultAllocator.Factory<>());
		pool.start();
		return pool;
	}
	
	private static GenericObjectPool<PoolObject2> createCommonsPool(int min, int max, long timeout) {
		BasePooledObjectFactory<PoolObject2> factory = new BasePooledObjectFactory<>() {
			@Override
			public PoolObject2 create() {
				return new PoolObject2();
			}
			
			@Override
			public PooledObject<PoolObject2> wrap(PoolObject2 obj) {
				return new DefaultPooledObject<>(obj);
			}
			
			@Override
			public boolean validateObject(PooledObject<PoolObject2> p) {
				return true;
			}
			
			@Override
			public void destroyObject(PooledObject<PoolObject2> p) {
			}
		};
		
		GenericObjectPoolConfig<PoolObject2> config = new GenericObjectPoolConfig<>();
		config.setMaxTotal(max);
		config.setMinIdle(min);
		config.setMaxWait(Duration.ofMillis(timeout));
		config.setTestOnBorrow(false);
		config.setTestOnReturn(false);
		config.setTestWhileIdle(false);
		config.setTimeBetweenEvictionRuns(Duration.ofMillis(60_000));
		config.setBlockWhenExhausted(true);
		config.setJmxEnabled(false);
		
		return new GenericObjectPool<>(factory, config);
	}
	
	public static void main(String[] args) throws Exception {
		int[] threadCounts = {1, 4, 8, 16};
		
		List<RunResult> allResults = new ArrayList<>();
		
		for (int threads : threadCounts) {
			Options options = new OptionsBuilder()
					.include(ObjectPoolBenchmark.class.getSimpleName())
					.threads(threads)
					.shouldDoGC(true)
					.build();
			
			allResults.addAll(new Runner(options).run());
		}
	}
}