package cn.nextop.lite.pool;


import static cn.nextop.lite.pool.PoolValidation.ACQUIRE;
import static cn.nextop.lite.pool.PoolValidation.PULSE;
import static cn.nextop.lite.pool.PoolValidation.RELEASE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Jingqi Xu
 */
public class PoolValidationTest {
	
	@Test
	public void test() {
		//
		PoolValidation v;
		v = new PoolValidation();
		assertFalse(v.isPulseEnabled());
		assertFalse(v.isAcquireEnabled());
		assertFalse(v.isReleaseEnabled());
		assertFalse(v.isEnabled( PULSE ));
		assertFalse(v.isEnabled(ACQUIRE));
		assertFalse(v.isEnabled(RELEASE));
		assertEquals( "0", v.toString() );
		assertEquals((byte)0, v.getValue());
		
		PoolValidation v1 = v.copy();
		Assertions.assertEquals(v, v);
		Assertions.assertEquals(v, v1);
		Assertions.assertNotEquals(v, null);
		Assertions.assertNotEquals(v, "tt");
		v1.setPulseEnabled(true); // pulse !
		Assertions.assertNotEquals( v, v1 );
		
		//
		PoolValidation v2 = null;
		v2 = v.copy(); v2.setPulseEnabled(true);
		Assertions.assertTrue( v2.isPulseEnabled() );
		Assertions.assertTrue( v2.isEnabled(PULSE) );
		Assertions.assertEquals( "1",v2.toString() );
		
		v2.setAcquireEnabled(true);
		Assertions.assertTrue(v2.isAcquireEnabled());
		Assertions.assertTrue(v2.isEnabled(ACQUIRE));
		Assertions.assertEquals("11",v2.toString( ));
		
		v2.setReleaseEnabled(true);
		Assertions.assertTrue(v2.isReleaseEnabled());
		Assertions.assertTrue(v2.isEnabled(RELEASE));
		Assertions.assertEquals("111",v2.toString());
		
		v2.setPulseEnabled(false);
		Assertions.assertFalse( v2.isPulseEnabled() );
		Assertions.assertFalse( v2.isEnabled(PULSE) );
		
		v2.setAcquireEnabled(false);
		Assertions.assertFalse(v2.isAcquireEnabled());
		Assertions.assertFalse(v2.isEnabled(ACQUIRE));
		
		v2.setReleaseEnabled(false);
		Assertions.assertFalse(v2.isReleaseEnabled());
		Assertions.assertFalse(v2.isEnabled(RELEASE));
		
		//
		v2.setEnabled(PULSE, true);
		Assertions.assertTrue( v2.isPulseEnabled() );
		Assertions.assertTrue( v2.isEnabled(PULSE) );
		
		v2.setEnabled(ACQUIRE, true);
		Assertions.assertTrue(v2.isAcquireEnabled());
		Assertions.assertTrue(v2.isEnabled(ACQUIRE));
		
		v2.setEnabled(RELEASE, true);
		Assertions.assertTrue(v2.isReleaseEnabled());
		Assertions.assertTrue(v2.isEnabled(RELEASE));
		
		v2.setEnabled(PULSE, false);
		Assertions.assertFalse( v2.isPulseEnabled() );
		Assertions.assertFalse( v2.isEnabled(PULSE) );
		
		v2.setEnabled(ACQUIRE, false);
		Assertions.assertFalse(v2.isAcquireEnabled());
		Assertions.assertFalse(v2.isEnabled(ACQUIRE));
		
		v2.setEnabled(RELEASE, false);
		Assertions.assertFalse(v2.isReleaseEnabled());
		Assertions.assertFalse(v2.isEnabled(RELEASE));
	}
}
