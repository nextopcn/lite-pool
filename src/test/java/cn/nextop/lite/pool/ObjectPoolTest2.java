package cn.nextop.lite.pool;

import static cn.nextop.lite.pool.ObjectPoolTest1.delayQuietly;
import static cn.nextop.lite.pool.ObjectPoolTest1.pool;
import static java.util.HashSet.newHashSet;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;

import cn.nextop.lite.pool.benchmark.entity.PoolObject1;
import cn.nextop.lite.pool.impl.ObjectPool;

/**
 * @author Baoyi Chen
 */
public class ObjectPoolTest2 {
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test1() {
		var p = pool(0, 3, 1000, 1000, 0, 0);
		p.start();
		try {
			for (int i = 0; i < 3; i++) {
				assertNotNull(p.acquire());
			}
			long st = System.nanoTime();
			assertNull(p.acquire());
			long ed = System.nanoTime();
			long elapsed = (ed - st) / 1_000_000;
			assertTrue(elapsed >= 950 && elapsed <= 1050);
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test2() {
		var p = pool(0, 3, 1000, 1000, 0, 0);
		p.start();
		try {
			for (int i = 0; i < 3; i++) {
				assertNotNull(p.acquire());
			}
			long st = System.nanoTime();
			assertNull(p.acquire());
			long ed = System.nanoTime();
			long elapsed = (ed - st) / 1_000_000;
			assertTrue(elapsed >= 950 && elapsed <= 1050);
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test3() {
		int n = 3, waiters = 6;
		final ObjectPool<PoolObject1> p;
		p = pool(n, n, 5000, 5000, 0, 0);
		p.start(); try {
			List<PoolObject1> held = new ArrayList<>();
			for (int i = 0; i < n; i++) held.add(p.acquire());
			
			AtomicInteger acquired = new AtomicInteger();
			AtomicInteger timedOut = new AtomicInteger();
			CountDownLatch started = new CountDownLatch(waiters);
			CountDownLatch done = new CountDownLatch(waiters);
			
			for (int i = 0; i < waiters; i++) {
				new Thread(() -> {
					started.countDown();
					try { started.await(); } catch (InterruptedException e) { return; }
					PoolObject1 o = p.acquire(1000, MILLISECONDS);
					if (o != null) {
						acquired.incrementAndGet();
					} else {
						timedOut.incrementAndGet();
					}
					done.countDown();
				}).start();
			}
			
			try { started.await(); } catch (InterruptedException e) { fail(e); }
			
			for (PoolObject1 o : held) {
				delayQuietly(100);
				p.release(o);
			}
			
			try { done.await(5, SECONDS); } catch (InterruptedException e) { fail(e); }
			
			assertEquals(3, acquired.get());
			assertEquals(3, timedOut.get());
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test4() {
		Set<Integer> validated = newHashSet(10);
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 0, 0);
		
		p.getConfig().setValidator((v, w) -> {
			validated.add(w.getId());
			return true;
		});
		p.getConfig().getValidation().setAcquireEnabled(true);
		
		p.start(); try {
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire();
				assertNotNull(o);
			}
			
			assertEquals(3, validated.size());
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test5() {
		CountDownLatch leaked = new CountDownLatch(2);
		final ObjectPool<PoolObject1> p;
		p = pool(0, 4, 1000, 500, 0, 0);
		p.getConfig().setTenancy(1000);
		p.addListener(event -> leaked.countDown());
		
		p.start(); try {
			PoolObject1 o1 = p.acquire();
			PoolObject1 o2 = p.acquire();
			assertNotNull(o1); assertNotNull(o2);
			
			try {
				assertTrue(leaked.await(5, SECONDS), "leak not detected in 5s");
			} catch (InterruptedException e) {
				fail(e);
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test6() {
		AtomicInteger destroyed = new AtomicInteger();
		AtomicInteger validated = new AtomicInteger();
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 0, 0);
		
		p.getConfig().setValidator((v, w) -> {
			validated.incrementAndGet();
			return false;
		});
		p.getConfig().setConsumer(v -> destroyed.incrementAndGet());
		p.getConfig().getValidation().setReleaseEnabled(true);
		p.getConfig().getValidation().setAcquireEnabled(false);
		
		p.start(); try {
			Set<PoolObject1> held = newHashSet(10);
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire();
				assertNotNull(o);
				held.add(o);
			}
			
			for (PoolObject1 o : held) {
				p.release(o);
			}
			
			assertEquals(3, validated.get());
			assertEquals(3, destroyed.get());
			
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(2000, MILLISECONDS);
				assertNotNull(o);
				assertFalse(held.contains(o), "should be a new object, not the destroyed one");
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test7() {
		int n = 8, ops = 1000;
		final ObjectPool<PoolObject1> p;
		p = pool(n, n, 1000, 5000, 0, 0);
		p.start(); try {
			for (int i = 0; i < ops; i++) {
				Set<PoolObject1> held = newHashSet(10);
				for (int j = 0; j < n; j++) {
					PoolObject1 o = p.acquire();
					assertNotNull(o);
					assertTrue(held.add(o), "duplicate object: " + o.getId());
				}
				assertNull(p.acquire(0, MILLISECONDS));
				for (PoolObject1 o : held) p.release(o);
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test8() {
		int threads = 4, ops = 2000;
		final ObjectPool<PoolObject1> p;
		p = pool(1, 1, 2000, 2000, 0, 0);
		
		AtomicInteger ok = new AtomicInteger();
		AtomicInteger fail = new AtomicInteger();
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		
		p.start(); try {
			for (int i = 0; i < threads; i++) {
				new Thread(() -> {
					try { start.await(); } catch (InterruptedException e) { return; }
					for (int j = 0; j < ops; j++) {
						PoolObject1 o = p.acquire(500, MILLISECONDS);
						if (o != null) {
							ok.incrementAndGet();
							p.release(o);
						} else {
							fail.incrementAndGet();
						}
					}
					done.countDown();
				}).start();
			}
			
			start.countDown();
			try { done.await(30, SECONDS); } catch (InterruptedException e) { fail(e); }
			
			assertEquals(0, fail.get(), "some acquires failed");
			assertEquals(threads * ops, ok.get());
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test9() {
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 1000, 0);
		p.start(); try {
			Set<PoolObject1> old = newHashSet(10);
			for (int i = 0; i < 3; i++) old.add(p.acquire());
			for (PoolObject1 o : old) p.release(o);
			
			delayQuietly(1600);
			
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(2000, MILLISECONDS);
				assertNotNull(o);
				assertFalse(old.contains(o), "TTL expired, should be new object");
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test10() {
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 0, 1000);
		p.start(); try {
			Set<PoolObject1> old = newHashSet(10);
			for (int i = 0; i < 3; i++) old.add(p.acquire());
			for (PoolObject1 o : old) p.release(o);
			
			delayQuietly(1600);
			
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(2000, MILLISECONDS);
				assertNotNull(o);
				assertFalse(old.contains(o), "TTI expired, should be new object");
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test11() {
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 0, 1000); // ttl=0, tti=1000
		p.start(); try {
			Set<PoolObject1> old = newHashSet(10);
			for (int i = 0; i < 3; i++) old.add(p.acquire());
			
			for (int round = 0; round < 3; round++) {
				for (PoolObject1 o : new ArrayList<>(old)) p.release(o);
				delayQuietly(800);
				old.clear();
				for (int i = 0; i < 3; i++) {
					PoolObject1 o = p.acquire(1000, MILLISECONDS);
					assertNotNull(o);
					old.add(o);
				}
			}
			
			assertEquals(3, old.size());
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test12() {
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 1000, 0); // ttl=1000, tti=0
		p.start(); try {
			Set<PoolObject1> old = newHashSet(10);
			for (int i = 0; i < 3; i++) old.add(p.acquire());
			for (PoolObject1 o : old) p.release(o);
			
			delayQuietly(500);
			
			Set<PoolObject1> same = newHashSet(10);
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(1000, MILLISECONDS);
				assertNotNull(o);
				same.add(o);
			}
			assertEquals(old, same);
			for (PoolObject1 o : same) p.release(o);
			
			delayQuietly(1200);
			
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(2000, MILLISECONDS);
				assertNotNull(o);
				assertFalse(old.contains(o), "TTL counts from creation, not last use");
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test13() {
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 5000, 1000);
		p.start(); try {
			Set<PoolObject1> old = newHashSet(10);
			for (int i = 0; i < 3; i++) old.add(p.acquire());
			for (PoolObject1 o : old) p.release(o);
			
			delayQuietly(1600);
			
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(2000, MILLISECONDS);
				assertNotNull(o);
				assertFalse(old.contains(o), "TTI triggers before TTL");
			}
		} finally {
			p.stop();
		}
	}
	
	@Execution(CONCURRENT)
	@RepeatedTest(value = 1)
	public void test14() {
		final ObjectPool<PoolObject1> p;
		p = pool(0, 3, 1000, 500, 1000, 0);
		p.start(); try {
			Set<PoolObject1> held = newHashSet(10);
			for (int i = 0; i < 3; i++) held.add(p.acquire());
			
			delayQuietly(1600);
			
			Set<PoolObject1> newOnes = newHashSet(10);
			for (PoolObject1 o : held) p.release(o);
			for (int i = 0; i < 3; i++) {
				PoolObject1 o = p.acquire(2000, MILLISECONDS);
				assertNotNull(o);
				newOnes.add(o);
			}
			
			assertFalse(newOnes.containsAll(held), "expired on release, got new objects");
		} finally {
			p.stop();
		}
	}
}
