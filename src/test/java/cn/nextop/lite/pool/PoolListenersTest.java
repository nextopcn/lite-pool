package cn.nextop.lite.pool;

import static cn.nextop.lite.pool.support.PoolAllocator.Slot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import cn.nextop.lite.pool.support.PoolAllocatorListener;
import cn.nextop.lite.pool.support.PoolAllocatorListeners;

/**
 * 
 * @author Jingqi Xu
 */
@SuppressWarnings("unchecked")
public class PoolListenersTest {
	
	/**
	 * 
	 */
	@Test
	public void test1() {
		//
		PoolListeners<String> v1;
		final var object = "test";
		v1 = new PoolListeners<>("");
		RuntimeException e;
		final PoolListener<String> x1, x2, x3;
		AtomicInteger c0 = new AtomicInteger();
		e = new RuntimeException("");
		
		//
		x1 = Mockito.mock(PoolListener.class);
		x2 = Mockito.mock(PoolListener.class);
		x3 = Mockito.mock(PoolListener.class);
		
		Answer<?> a0 = (v) -> c0.incrementAndGet();
		Mockito.doThrow(e).when(x1).onLeakage(any());
		Mockito.doAnswer(a0).when(x2).onLeakage(any());
		Mockito.doAnswer(a0).when(x3).onLeakage(any());
		
		//
		v1.onLeakage(object); assertEquals(0, c0.get());
		
		v1.addListener(x2);
		v1.onLeakage(object); assertEquals(1, c0.get());
		
		v1.addListener(x3);
		v1.onLeakage(object); assertEquals(3, c0.get());
		
		v1.addListener(x1);
		v1.onLeakage(object); assertEquals(5, c0.get());
		
		v1.delListener(x1);
		v1.delListener(x2);
		v1.onLeakage(object); assertEquals(6, c0.get());
	}
	
	/**
	 * 
	 */
	@Test
	public void test2() {
		//
		RuntimeException e;
		PoolAllocatorListeners<String> v1;
		PoolAllocatorListener<String> x1, x2, x3;
		
		v1 = new PoolAllocatorListeners<>("");
		e = new RuntimeException("");
		AtomicInteger c1 = new AtomicInteger();
		AtomicInteger c2 = new AtomicInteger();
		AtomicInteger c3 = new AtomicInteger();
		
		//
		final Slot<String> m = mock(Slot.class);
		x1 = Mockito.mock(PoolAllocatorListener.class);
		x2 = Mockito.mock(PoolAllocatorListener.class);
		x3 = Mockito.mock(PoolAllocatorListener.class);
		
		Answer <?> a1 = (v) -> c1.incrementAndGet();
		Answer <?> a2 = (v) -> c2.incrementAndGet();
		Answer <?> a3 = (v) -> c3.incrementAndGet();
		
		Mockito.doThrow(e).when(x1).onAcquire (any());
		Mockito.doThrow(e).when(x1).onRelease (any());
		Mockito.doThrow(e).when(x1).onLeakage(any());
		
		Mockito.doAnswer(a1).when(x2).onAcquire(any());
		Mockito.doAnswer(a2).when(x2).onRelease(any());
		Mockito.doAnswer(a3).when(x2).onLeakage(any());
		
		Mockito.doAnswer(a1).when(x3).onAcquire(any());
		Mockito.doAnswer(a2).when(x3).onRelease(any());
		Mockito.doAnswer(a3).when(x3).onLeakage(any());
		
		//
		v1.onAcquire(null); assertEquals(0, c1.get());
		v1.onRelease(null); assertEquals(0, c2.get());
		v1.onLeakage(null); assertEquals(0, c3.get());
		
		//
		v1.onAcquire(m); assertEquals ( 0, c1.get() );
		v1.onRelease(m); assertEquals ( 0, c2.get() );
		v1.onLeakage(m); assertEquals ( 0, c3.get() );
		
		//
		v1.addListener(x2);
		v1.onAcquire(m); assertEquals ( 1, c1.get() );
		v1.onRelease(m); assertEquals ( 1, c2.get() );
		v1.onLeakage(m); assertEquals ( 1, c3.get() );
		
		//
		v1.addListener(x3);
		v1.onAcquire(m); assertEquals ( 3, c1.get() );
		v1.onRelease(m); assertEquals ( 3, c2.get() );
		v1.onLeakage(m); assertEquals ( 3, c3.get() );
		
		//
		v1.addListener(x1);
		v1.onAcquire(m); assertEquals ( 5, c1.get() );
		v1.onRelease(m); assertEquals ( 5, c2.get() );
		v1.onLeakage(m); assertEquals ( 5, c3.get() );
		
		//
		v1.delListener(x1);
		v1.delListener(x2);
		v1.onAcquire(m); assertEquals ( 6, c1.get() );
		v1.onRelease(m); assertEquals ( 6, c2.get() );
		v1.onLeakage(m); assertEquals ( 6, c3.get() );
	}
}
