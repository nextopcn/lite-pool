package cn.nextop.lite.pool.benchmark.entity;

import static cn.nextop.lite.pool.util.Strings.buildEx;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Baoyi Chen
 */
public class PoolObject1 {
	//
	public static AtomicInteger ID = new AtomicInteger((0));
	
	//
	private final int id;
	
	public int getId() { return this.id; }
	
	public PoolObject1() { this.id = ID.getAndIncrement(); }
	
	@Override public String toString() { return buildEx(this); }
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		PoolObject1 that = (PoolObject1) o;
		return id == that.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
