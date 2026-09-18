package cn.nextop.lite.pool;

import static cn.nextop.lite.pool.support.PoolAllocator.Phase;
import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;

/**
 * 
 * @author Jingqi Xu
 */
public class PoolBuilderTest {
	
	/**
	 * 
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 2)
	public void test1() {
		//
		PoolBuilder<Object> b1;
		b1 = new PoolBuilder<>();
		
		b1.fifo(false);
		b1.validation(null);
		b1.tti(0L); b1.ttl(0L);
		b1.minimum(0); b1.maximum(0);
		b1.tenancy(0L); b1.timeout(0L);
		b1.interval(0L); b1.consumer(null);
		b1.supplier(null); b1.verbose(false);
		b1.validator(null); b1.allocator(null);
		
		Pool<Object> t1 = b1.build("test1");
		PoolConfig<Object> c1 = t1.getConfig();
		Assertions.assertEquals(0L, c1.getTti());
		Assertions.assertEquals(0L, c1.getTtl());
		Assertions.assertEquals(false, c1.isFifo());
		Assertions.assertEquals(0, c1.getMinimum());
		Assertions.assertEquals(0, c1.getMaximum());
		Assertions.assertEquals(0L, c1.getTenancy());
		Assertions.assertEquals(0L, c1.getTimeout());
		Assertions.assertEquals(0L, c1.getInterval());
		Assertions.assertEquals(false, t1.isVerbose());
		Assertions.assertEquals(null, c1.getConsumer());
		Assertions.assertEquals(null, c1.getSupplier());
		Assertions.assertEquals(null, c1.getValidator());
		Assertions.assertEquals(null, c1.getValidation());
		
		//
		PoolValidation x = new PoolValidation();
		final Supplier<Object> s1 = () -> (100);
		Consumer<Object> cc1 = (v) -> v.toString();
		BiPredicate<Phase, Object> p1 = (v, w) -> true;
		
		b1.fifo(true);
		b1.tti(1L); b1.ttl(2L);
		b1.minimum(3); b1.maximum(4);
		b1.tenancy(5L); b1.timeout(6L);
		b1.interval(7L); b1.consumer(cc1);
		b1.supplier(s1); b1.validation(x);
		b1.verbose(true); b1.validator(p1);
		
		Pool<Object> p2 = b1.build("test2");
		PoolConfig<Object> c2 = p2.getConfig();
		Assertions.assertEquals(1L, c2.getTti());
		Assertions.assertEquals(2L, c2.getTtl());
		Assertions.assertEquals(true, c2.isFifo());
		Assertions.assertEquals(3, c2.getMinimum());
		Assertions.assertEquals(4, c2.getMaximum());
		Assertions.assertEquals(5L, c2.getTenancy());
		Assertions.assertEquals(6L, c2.getTimeout());
		Assertions.assertEquals(7L, c2.getInterval());
		Assertions.assertEquals(true, p2.isVerbose());
		Assertions.assertEquals(s1, c2.getSupplier());
		Assertions.assertEquals(cc1, c2.getConsumer());
		Assertions.assertEquals(x, c2.getValidation());
		Assertions.assertEquals(p1, c2.getValidator());
	}
	
	/**
	 * 
	 */
	@Execution(CONCURRENT)
	@RepeatedTest(value = 2)
	public void test2() {
		
		final Random r = current();
		for(int i = 0; i < 32; i++) {
			
			var c = new PoolBuilder<Object>();
			PoolValidation p = new PoolValidation();
			final Supplier<Object> s1 = () -> (100);
			Consumer<Object> c1 = (v) -> v.toString();
			Long l1 = r.nextLong(), l2 = r.nextLong();
			Long l3 = r.nextLong(), l4 = r.nextLong();
			final int l6 = r.nextInt(Integer.MAX_VALUE);
			final int l7 = r.nextInt(Integer.MAX_VALUE);
			BiPredicate<Phase, Object> p1 = (v, w) -> true;
			boolean b1 = r.nextBoolean(), b2 = r.nextBoolean();
			
			c.fifo(b1);
			c.tti(l1); c.ttl(l2);
			c.minimum(l6); c.maximum(l7);
			c.tenancy(l3); c.timeout(l4);
			c.verbose(b2); c.validator(p1);
			c.interval(l4); c.consumer(c1);
			c.supplier(s1); c.validation(p);
			
			p.setPulseEnabled(r.nextBoolean());
			p.setAcquireEnabled(r.nextBoolean());
			p.setReleaseEnabled(r.nextBoolean());
			
			Pool<Object> p2 = c.build("rtest");
			PoolConfig<Object> c3 = p2.getConfig();
			Assertions.assertEquals(l1, c3.getTti());
			Assertions.assertEquals(l2, c3.getTtl());
			Assertions.assertEquals(b1, c3.isFifo());
			Assertions.assertEquals(b2, p2.isVerbose());
			Assertions.assertEquals(l6, c3.getMinimum());
			Assertions.assertEquals(l7, c3.getMaximum());
			Assertions.assertEquals(l3, c3.getTenancy());
			Assertions.assertEquals(l4, c3.getTimeout());
			Assertions.assertEquals(l4, c3.getInterval());
			Assertions.assertEquals(c1, c3.getConsumer());
			Assertions.assertEquals(s1, c3.getSupplier());
			Assertions.assertEquals(p1, c3.getValidator());
			assertEquals(p.getValue(), c3.getValidation().getValue());
		}
	}
}
