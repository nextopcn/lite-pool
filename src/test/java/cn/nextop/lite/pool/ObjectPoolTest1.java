package cn.nextop.lite.pool;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;

import cn.nextop.lite.pool.benchmark.entity.PoolObject1;
import cn.nextop.lite.pool.impl.ObjectPool;
import cn.nextop.lite.pool.support.allocator.DefaultAllocator;

/**
 * @author Baoyi Chen
 */
public class ObjectPoolTest1 {
	
	/**
	 * 
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test1() {
		//
		int n = 8;
		TimeUnit u = MILLISECONDS;
		final ObjectPool<PoolObject1> p;
		p = pool(1, n, 1000, 1000, 0, 0);
		
		//
		p.start(); try {
			//
			PoolObject1 o;
			List<PoolObject1> x;
			x = new ArrayList<>();
			assertNotNull(p.toString());
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull (o); x.add(o);
			}
			
			//
			assertNull ( p.acquire(0L, u) );
			assertNull ( p.acquire(0L, u) );
			
			//
			p.release(x.remove(0));
			assertNotNull(p.acquire(0L, u));
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test2() {
		//
		int n = 8;
		TimeUnit u = MILLISECONDS;
		final ObjectPool <PoolObject1> p;
		p = pool(n, n, 1000, 1000, 0, 0);
		
		//
		p.start(); try {
			//
			PoolObject1 o;
			Set<PoolObject1> x;
			x = new HashSet<>();
			assertNotNull(p.toString());
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull (o); x.add(o);
			}
			
			//
			assertNull ( p.acquire(0L, u) );
			assertNull ( p.acquire(0L, u) );
			for(final PoolObject1 obj : x) {
				p.release(obj);
			}
			
			//
			delayQuietly(1200);
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull(o);
				assertTrue( x.contains(o) );
			}
		} finally {
			p.stop();
		}
	}
	
	/**
	 * TTL & TTI
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test3() {
		//
		int n = 8;
		int ttl = 1000;
		TimeUnit u = MILLISECONDS;
		final ObjectPool<PoolObject1> p;
		p = pool(1, n, 1000, ttl, ttl, 0);
		
		//
		p.start(); try {
			//
			PoolObject1 o;
			Set<PoolObject1> x;
			x = new HashSet<>();
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull (o); x.add(o);
			}
			
			//
			assertNull ( p.acquire(0L, u) );
			assertNull ( p.acquire(0L, u) );
			for(final PoolObject1 obj : x) {
				p.release(obj);
			}
			
			//
			delayQuietly(ttl * 2 + 200);
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull (o);
				assertFalse (x.contains(o));
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test4() {
		//
		int n = 8;
		int tti = 1000;
		TimeUnit u = MILLISECONDS;
		final ObjectPool<PoolObject1> p;
		p = pool(1, n, 1000, tti, 0, tti);
		
		//
		p.start(); try {
			//
			PoolObject1 o;
			Set<PoolObject1> x;
			x = new HashSet<>();
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull (o); x.add(o);
			}
			
			//
			assertNull ( p.acquire(0L, u) );
			assertNull ( p.acquire(0L, u) );
			for(final PoolObject1 obj : x) {
				p.release(obj);
			}
			
			//
			delayQuietly(tti * 2 + 200);
			for(int i = 0; i < n; i++) {
				o = p.acquire();
				assertNotNull (o);
				assertFalse (x.contains(o));
			}
		} finally {
			p.stop();
		}
	}
	
	/**
	 * 
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test5() {
		//
		final CountDownLatch latch;
		latch = new CountDownLatch(3);
		final ObjectPool<PoolObject1> p;
		
		p = pool(3, 3, 1000, 1000, 0, 0);
		p.getConfig().setTenancy (2000L);
		p.addListener(v -> { latch.countDown(); });
		
		//
		p.start(); try {
			for(int i = 0 ; i < 5 ; i++) {
				p.acquire();
			}
			try { latch.await(3, SECONDS);
			} catch(Throwable root) { fail(root); }
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test6() {
		//
		final ObjectPool<PoolObject1> p;
		p = pool(3, 3, 1000, 5000, 0, 0);
		
		PoolConfig<PoolObject1> c = p.getConfig();
		c.setValidator((v , w) -> w.getId() != 1);
		c.getValidation().setAcquireEnabled(true);
		c.setConsumer(v -> assertEquals(1, v.getId()));
		
		//
		p.start(); try {
			for (int i = 0; i < 5; i++) {
				PoolObject1 o = null;
				try { o = p.acquire();
				} finally { p.release(o); } /** nop **/
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test7() {
		//
		final ObjectPool<PoolObject1> p;
		p = pool(3, 3, 1000, 5000, 0, 0);
		
		PoolConfig<PoolObject1> c = p.getConfig();
		c.setValidator((v , w) -> w.getId() != 1);
		c.getValidation().setReleaseEnabled(true);
		c.setConsumer(v -> assertEquals(1, v.getId()));
		
		//
		p.start(); try {
			for (int i = 0; i < 5; i++) {
				PoolObject1 o = null;
				try { o = p.acquire();
				} finally { p.release(o); } /** nop **/
			}
		} finally {
			p.stop();
		}
	}
	
	/**
	 * Concurrency
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test11() {
		//
		int n = 16, n1 = 8, n2 = 10000;
		final ObjectPool<PoolObject1> p;
		p = pool(1, n, 1000, 5000, 0, 0);
		
		AtomicInteger c1 = new AtomicInteger();
		AtomicInteger c2 = new AtomicInteger();
		CountDownLatch x = new CountDownLatch(n1);
		p.setFactory(new DefaultAllocator.Factory<>());
		
		//
		p.start(); try {
			//
			for(int i = 0; i < n1; i++) {
				new Thread(() -> {
					for(int j = 0; j < n2; j++) {
						PoolObject1 o = null;
						try {
							o = p.acquire();
							if (o != null) {
								c1.incrementAndGet();
							} else {
								c2.incrementAndGet();
							}
						} finally {
							p.release(o); /** nop **/
						}
					}
					x.countDown();
				}).start();
			}
			
			//
			int total = n1 * n2;
			try { x.await(); } catch(Throwable e) {}
			Assertions.assertEquals( 0 , c2.get() );
			Assertions.assertEquals(total, c1.get());
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test12() {
		//
		int n = 8, n1 = 16, n2 = 10000;
		final ObjectPool<PoolObject1> p;
		p = pool(1, n, 1000, 5000, 0, 0);
		
		AtomicInteger c1 = new AtomicInteger();
		AtomicInteger c2 = new AtomicInteger();
		CountDownLatch x = new CountDownLatch(n1);
		
		//
		p.start(); try {
			//
			for(int i = 0; i < n1; i++) {
				new Thread(() -> {
					for(int j = 0; j < n2; j++) {
						PoolObject1 o = null;
						try {
							o = p.acquire();
							if (o != null) {
								c1.incrementAndGet();
							} else {
								c2.incrementAndGet();
							}
						} finally {
							p.release(o); /** nop **/
						}
					}
					x.countDown();
				}).start();
			}
			
			//
			int total = n1 * n2;
			try { x.await(); } catch(Throwable e) {}
			Assertions.assertEquals( 0 , c2.get() );
			Assertions.assertEquals(total, c1.get());
		} finally {
			p.stop();
		}
	}
	
	/**
	 * 
	 */
	static ObjectPool<PoolObject1> pool(
		int min, int max, long timeout, 
		long interval, long ttl, long tti, boolean fifo) {
		//
		var c = new PoolConfig<PoolObject1>();
		c.setValidator((v, w) -> true); /* NOP */
		c.setMinimum(min); c.setTimeout(timeout);
		c.setMaximum(max); c.setInterval(interval);
		c.setTtl(ttl); c.setTti(tti); c.setFifo(fifo);
		
		c.setConsumer (v -> {
			// System.out.println("deleted object: " + v);
		});
		
		c.setSupplier(() -> {
			PoolObject1 t = new PoolObject1();
			// System.out.println("created object: " + t);
			return t;
		});
		
		//
		ObjectPool<PoolObject1> r = new ObjectPool<>("x");
		r.setVerbose(true); r.setConfig(c); return r;/*!*/
	}
	
	static ObjectPool <PoolObject1> pool(int min, int max,
		long timeout, long interval, long ttl, long tti) {
		return pool(min, max, timeout, interval, ttl, tti, false);
	}
	
	static void delayQuietly(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
}
