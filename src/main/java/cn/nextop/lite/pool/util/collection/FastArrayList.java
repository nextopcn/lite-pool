package cn.nextop.lite.pool.util.collection;

import static cn.nextop.lite.pool.util.Arrays.fill;
import static cn.nextop.lite.pool.util.Arrays.newArray;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;
import static java.util.Objects.requireNonNull;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;

import cn.nextop.lite.pool.glossary.Copyable;
import cn.nextop.lite.pool.glossary.Nullable;
import cn.nextop.lite.pool.glossary.Required;
import cn.nextop.lite.pool.glossary.Sizable;

/**
 * @author Baoyi Chen
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class FastArrayList<T> extends AbstractList<T> implements RandomAccess, Copyable<FastArrayList<T>> {
	//
	protected int size;
	protected Object[] array;
	
	//
	static final Object[] NIL = {};
	
	static final int INITIAL = 10; /*!*/
	
	static final int MAXIMUM = MAX_VALUE - 8;
	
	/**
	 * 
	 */
	public FastArrayList() {
		this.array = NIL; /**!**/
	}
	
	public FastArrayList(int n) {
		array = n <= 0 ? NIL : new Object[n];
	}
	
	public FastArrayList(
		FastArrayList<? extends T> v) {
		final int n = v.size; this.size = n ;
		if( n == 0 ) { array = NIL; return; }
		array = copyOfRange(v.array , 0 , n);
	}
	
	public FastArrayList(
		Collection <? extends T> rhs) {
		final var a = rhs.toArray(NIL); /*!*/
		this.array = a; this.size = a.length;
	}
	
	public FastArrayList(final T[] v) {
		int n = ((v == null) ? 0 : v.length);
		if( n == 0 ) { array = NIL; return; }
		array = copyOf(v , n); this.size = n;
	}
	
	public FastArrayList(
		@Required T[] v, int i, int n) {
		if( n <= 0 ) { array = NIL; return; }
		this.size = n; int x = i , y = i + n;
		array = Arrays.copyOfRange (v, x, y);
	}
	
	/**
	 * 
	 */
	@Override
	public int size () {
		return this.size;
	}
	
	public int capacity() {
		return array.length;
	}
	
	@Override
	public T get (int index) {
		return (T)array[index];
	}
	
	@Override
	public boolean contains(Object v) {
		return this.indexOf ( v ) >= 0;
	}
	
	@Override
	public final FastArrayList<T> copy() {
		return new FastArrayList<T>(this);
	}
	
	@Override
	public void clear() {
		int n = this.size; if(n == 0) return;
		fill(array , 0 , n , null); size = 0;
	}
	
	public void clear(int n) {
		size = 0; /** resize the capacity **/
		array = n <= 0 ? NIL : new Object[n];
	}
	
	@Override
	public T set(int i, T v) {
		validate ( i , true ); var a = array;
		var r = a[i]; a[i] = v; return (T) r;
	}
	
	@Override
	public boolean add (T v) {
		int n = size; var a = resize (n + 1);
		a[n] = v; size = n + 1; return true ;
	}
	
	/**
	 * 
	 */
	@Override
	public Object[] toArray() {
		int n = size; if (n == 0) return NIL;
		var a = array; var r = new Object[n];
		arraycopy( a, 0, r, 0, n ); return r;
	}
	
	@Override
	public <E> E[] toArray(E[] o) {
		int n = size; var r = newArray(o, n);
		System.arraycopy (array, 0, r, 0, n);
		return (E[]) r; /* Arrays.newArray */
	}
	
	/**
	 * 
	 */
	@Override
	public final Iterator<T> iterator() {
		return new It32();/*** @see It32 ***/
	}
	
	@Override
	public ListIterator<T> listIterator() {
		return new Itx32(0);/*** @see Itx32 ***/
	}
	
	@Override
	public ListIterator<T> listIterator(int i) {
		return new Itx32(i);/*** @see Itx32 ***/
	}
	
	@Override
	public void sort (Comparator<? super T> c) {
		int n = this.size; if(n <= 1) return;
		Arrays.sort((T[])(this.array), 0, n, c);
	}
	
	/**
	 * 
	 */
	@Override
	public int indexOf ( Object v ) {
		final var array = this.array;
		for(int i = 0, n = size; i < n; i++) {
			if (Objects.equals(array[i] , v)) return i;
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object v) {
		final var array = this.array; 
		for(int i = (size - 1); i >= 0; i--) {
			if (Objects.equals(array[i] , v)) return i;
		}
		return -1;
	}
	
	public final void trimToSize() {
		int n = size; if(array.length == n) { return; }
		Object[] backup = array; array = new Object[n];
		System.arraycopy (backup, 0, this.array, 0, n);
	}
	
	@Override public void add(int i , T v) {
		validate(i, false); var n = this.size;
		final var a = resize( n + 1 ); int m = (n - i);
		if (m > 0) System.arraycopy(a, i, a, i + 1, m);
		a[i] = v; size = (n + 1); /*** shift & set ***/
	}
	
	@Override public T remove(final int i) {
		validate(i, true); var a = array; int n = size;
		final Object r = a[i]; final int m = n - i - 1;
		if (m > 0) System.arraycopy(a, i + 1, a, i, m);
		a[n - 1] = null; size = (n - 1); return (T)(r);
	}
	
	@Override
	protected void removeRange(int from , int to) {
		if(from > to || to < 1) return; /** nop **/
		var a = array; int m = to - from; int n = size;
		arraycopy(a, to, a, from, n - to); size -= (m);
		for(int i = this.size; i < n; i++) a[i] = null;
	}
	
	@Override
	public void forEach(final Consumer <? super T> o) {
		int n = size; if(n <= 0) return; var a = array;
		for(int i = 0; i < n; i++) { o.accept((T)a[i]); }
	}
	
	public static <T> FastArrayList<T> of (final T... a) {
		requireNonNull(a); var r = new FastArrayList<T>();
		r.size = a.length; r.array = a; /** ! **/ return r;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean addAll (Collection<? extends T> c) {
		//
		final var fast = (c instanceof FastArrayList);
		if(fast) return this.addAll((FastArrayList) c);
		
		//
		if (c == null) { return false; } /* optional */
		final var a = c.toArray(NIL); int n = a.length;
		if (n == 0) return false; this.resize(size + n);
		System.arraycopy(a, 0, array, size, n); size += n;
		return true;
	}
	
	@Override
	public boolean addAll(int i, Collection<? extends T> c) {
		//
		final boolean fast = (c instanceof FastArrayList);
		if(fast) { return this.addAll(i, (FastArrayList)c); }
		
		//
		if (c == null) return false; this.validate(i, false);
		final var a = c.toArray(NIL); final int n = a.length;
		if (n == 0) return false; this.resize(this.size + n);
		final Object[] v = this.array; int w = this.size - i;
		if(w > 0) { System.arraycopy(v , i , v , i + n , w); }
		arraycopy(a, 0, v, i, n); this.size += n; return true;
	}
	
	/**
	 * 
	 */
	public boolean addAll (final FastArrayList<? extends T> c) {
		if(c == null || c.size <= 0) return false;/* optional */
		var a = c.array; final int n = c.size; resize(size + n);
		arraycopy(a, 0, array, size, n); size += n; return true;
	}
	
	public boolean addAll(int i, FastArrayList<? extends T> c) {
		if(c == null || c.size <= 0) return false;/* optional */
		this.validate(i, false); var a = c.array; int n = c.size;
		this.resize(this.size + n); final Object[] v = this.array;
		int w = size - i; if(w > 0) { arraycopy(v, i, v, i + n, w); }
		System.arraycopy(a, 0, v, i, n); this.size += n; return true;
	}
	
	/**
	 * 
	 */
	protected final Object[] resize(final int capacity) {
		var a = array; int n = a.length; if (capacity <= n) return a;
		return this.array = Arrays.copyOf(a, this.grow(n, capacity));
	}
	
	protected final int grow(final int p , final int n) {
		if (n < 0 || p == MAXIMUM) { throw new OutOfMemoryError (); }
		if (p == 0 && n <= INITIAL) /* default */ { return INITIAL; }
		int r = max(p + (p >> 1) + 1, n); return r > 0 ? r : MAXIMUM;
	}
	
	protected void validate(final int index, final boolean inclusive) {
		if (index < 0) throw new IndexOutOfBoundsException("negative");
		if (inclusive && index >= size) throw new IndexOutOfBoundsException();
		else if (index > this.size) { throw new IndexOutOfBoundsException(); }
	}
	
	/**
	 * 
	 */
	protected class Itx32 implements ListIterator<T> {
		
		int next, last = -1; Itx32(int i) { this.next = i; }
		
		@Override public int nextIndex() { return this.next; }
		
		@Override public int previousIndex() { return this.next - 1; }
		
		@Override public boolean hasPrevious() { return this.next > 0; }
		
		@Override public boolean hasNext () { return (this.next < size); }
		
		@Override public void set(T v) { FastArrayList.this.set(this.last , v); }
		
		@Override public final T previous () {/*** moves the cursor backward ***/
			
			int i = this.next - 1; this.next = i; return (T)array[this.last = i];
		}
		
		@Override public final void add(@Nullable T v) {/*** appends element ***/
			
			int i = next; FastArrayList.this.add(i , v); next = i + 1; last = -1;
		}
		
		@Override public final T next() { return (T) array[ this.last = (this.next++) ]; }
		
		@Override public void remove () { if (last < 0) throw new IllegalStateException();
		
			FastArrayList.this.remove( this.last ); this.next = this.last; this.last = -1;
		}
	}
	
	protected class It32 implements Sizable, Iterator<T> {
		
		int next, last = -1; @Override public boolean hasNext () { return (next < size); }
		
		@Override public final int size() { return (size - this.next); } /*** Sizable ***/
		
		@Override public final T next() { return (T) array[ this.last = (this.next++) ]; }
		
		@Override public void remove () { if (last < 0) throw new IllegalStateException();
			
			FastArrayList.this.remove( this.last ); this.next = this.last; this.last = -1;
		}
	}
	
	/**
	 * 
	 */
	public boolean adds(T v1, T v2) {
		var a = array; int n = size, s = (n + 2); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; /*                        */ this.size = s; return true;
	}
	
	public boolean adds(T v1, T v2, T v3) {
		var a = array; int n = size, s = (n + 3); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; a[n++] = v3; /*           */ this.size = s; return true;
	}
	
	public boolean adds(T v1, T v2, T v3, T v4) {
		var a = array; int n = size, s = (n + 4); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; a[n++] = v3; a[n++] = v4; /*   */ this.size = s; return true;
	}
	
	public boolean adds(T v1, T v2, T v3, T v4, T v5) {
		var a = array; int n = size, s = (n + 5); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; a[n++] = v3; a[n++] = v4; a[n++] = v5; this.size = n; return true;
	}
	
	public boolean adds(T v1, T v2, T v3, T v4, T v5, T v6) {
		var a = array; int n = size, s = (n + 6); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; a[n++] = v3; a[n++] = v4; a[n++] = v5; a[n++] = v6; size = n; return true;
	}
	
	public boolean adds(T v1, T v2, T v3, T v4, T v5, T v6, T v7) {
		var a = array; int n = size, s = (n + 7); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; a[n++] = v3; a[n++] = v4; a[n++] = v5; a[n++] = v6; a[n++] = v7; size = n; return true;
	}
	
	public boolean adds(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8) {
		var a = array; int n = size, s = (n + 8); if(s > a.length) { a = this.resize(s); }
		a[n++] = v1; a[n++] = v2; a[n++] = v3; a[n++] = v4; a[n++] = v5; a[n++] = v6; a[n++] = v7; a[n++] = v8; size = n; return true;
	}
}
