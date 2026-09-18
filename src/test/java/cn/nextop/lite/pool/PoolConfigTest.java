package cn.nextop.lite.pool;

import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;

import static cn.nextop.lite.pool.support.PoolAllocator.Phase;

/**
 * 
 * @author Jingqi Xu
 */
public class PoolConfigTest {
	
	/**
	 * 
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 2)
	public void test1() {
		//
		PoolConfig<Object> c;
		c = new PoolConfig<>();
		assertFalse(c.isFifo());
		assertNull(c.getConsumer());
		assertNull(c.getSupplier());
		assertNull(c.getValidator());
		assertNotNull(c.getValidation());
		assertEquals(0L, c.getMinimum());
		assertEquals(16L, c.getMaximum());
		assertEquals(MINUTES.toMillis(15), c.getTti());
		assertEquals(MINUTES.toMillis(60), c.getTtl());
		assertEquals(MINUTES.toMillis(1), c.getTenancy());
		assertEquals(SECONDS.toMillis(5), c.getTimeout());
		assertEquals((byte)1, c.getValidation().getValue());
		assertEquals(SECONDS.toMillis(60), c.getInterval());
		
		//
		PoolValidation p;
		p = new PoolValidation();
		Supplier<Object> s1 = () -> 100;
		Consumer<Object> c1 = (v) -> v.toString();
		BiPredicate<Phase, Object> p1 = (v, w) -> true;
		
		c.setFifo(true);
		p.setPulseEnabled(false);
		p.setAcquireEnabled(true);
		p.setReleaseEnabled(true);
		c.setTti(1L); c.setTtl(2L);
		c.setMinimum(3); c.setMaximum(4);
		c.setTenancy(5L); c.setTimeout(6L);
		c.setConsumer(c1); c.setSupplier(s1);
		c.setValidation(p); c.setValidator(p1);
		c.setInterval(7L); c.setCookie(1, 100);
		
		Assertions.assertTrue(c.isFifo());
		Assertions.assertEquals(1L, c.getTti());
		Assertions.assertEquals(2L, c.getTtl());
		Assertions.assertEquals(3, c.getMinimum());
		Assertions.assertEquals(4, c.getMaximum());
		Assertions.assertEquals(5L, c.getTenancy());
		Assertions.assertEquals(6L, c.getTimeout());
		Assertions.assertEquals(c1, c.getConsumer());
		Assertions.assertEquals(s1, c.getSupplier());
		Assertions.assertEquals(7L, c.getInterval());
		Assertions.assertEquals(p1, c.getValidator());
		assertEquals(Integer.valueOf(100), c.getCookie(1));
		assertEquals(p.getValue(), c.getValidation().getValue());
	}
	
	/**
	 * 
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 2)
	public void test2() {
		
		final Random r = current();
		for(int i = 0; i < 32; i++) {
			
			var b1 = r.nextBoolean();
			var p = new PoolValidation();
			var c = new PoolConfig<Object>();
			final Supplier<Object> s1 = () -> (100);
			Consumer<Object> c1 = (v) -> v.toString();
			Long l1 = r.nextLong(), l2 = r.nextLong();
			Long l5 = r.nextLong(), l6 = r.nextLong();
			final int i3 = r.nextInt(Integer.MAX_VALUE);
			final int i4 = r.nextInt(Integer.MAX_VALUE);
			BiPredicate<Phase, Object> p1 = (v, w) -> true;
			
			c.setFifo(b1);
			c.setTti(l1); c.setTtl(l2);
			c.setMinimum(i3); c.setMaximum(i4);
			c.setTenancy(l5); c.setTimeout(l6);
			c.setConsumer(c1); c.setSupplier(s1);
			c.setValidation(p); c.setValidator(p1);
			c.setInterval(l6); c.setCookie(i4, i4);
			
			PoolConfig<Object> v2;
			v2 = c.copy(); eq(c, v2, i4);
			
			p.setPulseEnabled(r.nextBoolean());
			p.setAcquireEnabled(r.nextBoolean());
			p.setReleaseEnabled(r.nextBoolean());
			
			Assertions.assertEquals(l1, c.getTti());
			Assertions.assertEquals(l2, c.getTtl());
			Assertions.assertEquals(b1, c.isFifo());
			Assertions.assertEquals(i3, c.getMinimum());
			Assertions.assertEquals(i4, c.getMaximum());
			Assertions.assertEquals(l5, c.getTenancy());
			Assertions.assertEquals(l6, c.getTimeout());
			Assertions.assertEquals(c1, c.getConsumer());
			Assertions.assertEquals(s1, c.getSupplier());
			Assertions.assertEquals(l6, c.getInterval());
			Assertions.assertEquals(p1, c.getValidator());
			assertEquals(Integer.valueOf(i4), c.getCookie(i4));
			assertEquals(p.getValue(), c.getValidation().getValue());
		}
	}
	
	/**
	 * 
	 */
	protected void eq(PoolConfig<Object> c1, PoolConfig<Object> c2, int key) {
		Assertions.assertEquals(c1.getTti(), c2.getTti());
		Assertions.assertEquals(c1.getTtl(), c2.getTtl());
		Assertions.assertEquals(c1.isFifo(), c2.isFifo());
		Assertions.assertEquals(c1.getMinimum(), c2.getMinimum());
		Assertions.assertEquals(c1.getMaximum(), c2.getMaximum());
		Assertions.assertEquals(c1.getTenancy(), c2.getTenancy());
		Assertions.assertEquals(c1.getTimeout(), c2.getTimeout());
		Assertions.assertEquals(c1.getConsumer(), c2.getConsumer());
		Assertions.assertEquals(c1.getSupplier(), c2.getSupplier());
		Assertions.assertEquals(c1.getInterval(), c2.getInterval());
		Assertions.assertEquals(c1.getValidator(), c2.getValidator());
		Assertions.assertEquals(c1.getValidation(), c2.getValidation());
		Assertions.assertEquals((int)c1.getCookie(key), (int)c2.getCookie(key));
	}
}
