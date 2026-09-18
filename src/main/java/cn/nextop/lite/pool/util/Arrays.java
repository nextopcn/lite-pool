package cn.nextop.lite.pool.util;

import static java.lang.System.arraycopy;

import java.lang.reflect.Array;
import java.util.Collection;

import cn.nextop.lite.pool.glossary.Required;

/**
 * 
 * @author Jingqi Xu
 */
public final class Arrays {
	
	/**
	 * 
	 */
	public static int length(int[] a) {
		return a == null ? 0 : a.length;
	}
	
	public static int length(byte[] a) {
		return a == null ? 0 : a.length;
	}
	
	public static int length(char[] a) {
		return a == null ? 0 : a.length;
	}
	
	public static int length(long[] a) {
		return a == null ? 0 : a.length;
	}
	
	public static int length(short[] a) {
		return (a == null) ? 0 : a.length;
	}
	
	public static int length(float[] a) {
		return (a == null) ? 0 : a.length;
	}
	
	public static int length(double[] a) {
		return (a == null) ? 0 : a.length;
	}
	
	public static <T> int length (T[] a) {
		return (a == null) ? 0 : a.length;
	}
	
	/**
	 * 
	 */
	public static boolean isEmpty(int[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(byte[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(char[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(long[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(short[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(float[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(double[] a) {
		return a == null || a.length == 0;
	}
	
	public static <T> boolean isEmpty (T[] a) {
		return a == null || a.length == 0;
	}
	
	/**
	 * 
	 */
	public static String toString(int[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(byte[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(char[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(long[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(short[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(float[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(double[] a) {
		return java.util.Arrays.toString(a);
	}
	
	public static String toString(Object[] a) {
		return java.util.Arrays.toString(a);
	}
	
	/**
	 * 
	 */
	public static <T> void fill(
		final T[] a, int from, int to, T v) {
		for (int i = from; i < to; i++) a[i] = v;
	}
	
	public static <T> void fill(T[] a, T v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(int[] a, int v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(byte[] a, byte v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(char[] a, char v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(long[] a, long v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(short[] a, short v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(float[] a, float v) {
		final int n = (a == null ? 0 : a.length);
		for (int i = 0; i < n; i++) { a[i] = v; }
	}
	
	public static void fill(double[] a, double v) {
		final int n = ((a == null ? 0 : a.length));
		for (int i = 0; i < n; i++) { a[i] = (v); }
	}
	
	/**
	 * 
	 */
	public static <T> T get(T[] a, int i, T v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static int get(int[] a, int i, int v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static int get(byte[] a, int i, byte v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static int get(char[] a, int i, char v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static long get(long[] a, int i, long v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static short get(short[] a, int i, short v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static float get(float[] a, int i, float v) {
		if (a == null) return v; final int n = a.length;
		if (i >= 0 && i < n) return a[i]; else return v;
	}
	
	public static double get(double[] a, int i, double v) {
		if(a == null) { return v; } final int n = a.length;
		if(i >= 0 && i < n) { return a[i]; } else return v;
	}
	
	/**
	 * 
	 */
	public static byte[] toByteArray(Collection<Byte> c) {
		if(c == null) return null; byte[] r = new byte[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	public static long[] toLongArray(Collection<Long> c) {
		if(c == null) return null; long[] r = new long[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	public static short[] toShortArray(Collection<Short> c) {
		if(c == null) return null; short[] r = new short[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	public static float[] toFloatArray(Collection<Float> c) {
		if(c == null) return null; float[] r = new float[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	public static char[] toCharArray(Collection<Character> c) {
		if(c == null) { return null; } char[] r = new char[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	public static double[] toDoubleArray(Collection<Double> c) {
		if(c == null) return null; double[] r = new double[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	public static int[] toIntArray(final Collection<Integer> c) {
		if(c == null) return null; final int[] r = new int[c.size()];
		int i = 0; for(final var value : c) r[i++] = value; return r;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(final Class<?> type, int length) {
		return (T[])Array.newInstance(type, length); /* @see Array */
	}
	
	public static <T> T[] newArray(@Required T[] array, int length) {
		if(array.length == length/* the same length */) return array;
		return newArray(array.getClass().getComponentType(), length);
	}
	
	/**
	 * 
	 */
	public static CharSequence toBinaryString (byte[] a) {
		final int n = length(a); var r = new StringBuilder(n << 3);
		for(int i = 0; i < n; i++) { var v = Integer.toBinaryString(a[(i)]);
			for (int j = v.length(); j < 8; j++) r.append('0'); r.append(v);
		}
		return r;
	}
	
	public static CharSequence toBinaryString(short[] a) {
		final int n = length(a); var r = new StringBuilder(n << 4);
		for(int i = 0; i < n; i++) { var v = Integer.toBinaryString(a[(i)]);
			for(int j = v.length(); j < 16; j++) r.append('0'); r.append(v);
		}
		return r;
	}
	
	public static CharSequence toBinaryString (final int[] a) {
		final int n = length(a); var r = new StringBuilder(n << 5);
		for(int i = 0; i < n; i++) { var v = Integer.toBinaryString(a[(i)]);
			for(int j = v.length(); j < 32; j++) r.append('0'); r.append(v);
		}
		return r;
	}
	
	public static CharSequence toBinaryString(final long[] a) {
		final int n = length(a); var r = new StringBuilder(n << 6);
		for(int i = 0; i < n; i++) { String v = Long.toBinaryString(a[(i)]);
			for(int j = v.length(); j < 64; j++) r.append('0'); r.append(v);
		}
		return r;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("FallThrough")
	public static final void copy( int[] s, int i, int[] d, int j, int n ) {
		
		if (s == d || n > 32) { arraycopy(s, i, d, j, n);} else switch (n) {
		case 16: d[j + 15] = s[i + 15]; case 15: d[j + 14] = s[i + 14];
		case 14: d[j + 13] = s[i + 13]; case 13: d[j + 12] = s[i + 12];
		case 12: d[j + 11] = s[i + 11]; case 11: d[j + 10] = s[i + 10];
		case 10: d[j +  9] = s[i +  9]; case  9: d[j +  8] = s[i +  8];
		case  8: d[j +  7] = s[i +  7]; case  7: d[j +  6] = s[i +  6];
		case  6: d[j +  5] = s[i +  5]; case  5: d[j +  4] = s[i +  4];
		case  4: d[j +  3] = s[i +  3]; case  3: d[j +  2] = s[i +  2];
		case  2: d[j +  1] = s[i +  1]; case  1: d[j] = s[i]; break; /**!**/
		default: for(int k = n - 1; k >= 0; k--) d[j + k] = s[i + k]; break;
		}
	}
	
	@SuppressWarnings("FallThrough")
	public static final void copy(byte[] s, int i, byte[] d, int j, int n) {
		
		if (s == d || n > 32) { arraycopy(s, i, d, j, n);} else switch (n) {
		case 16: d[j + 15] = s[i + 15]; case 15: d[j + 14] = s[i + 14];
		case 14: d[j + 13] = s[i + 13]; case 13: d[j + 12] = s[i + 12];
		case 12: d[j + 11] = s[i + 11]; case 11: d[j + 10] = s[i + 10];
		case 10: d[j +  9] = s[i +  9]; case  9: d[j +  8] = s[i +  8];
		case  8: d[j +  7] = s[i +  7]; case  7: d[j +  6] = s[i +  6];
		case  6: d[j +  5] = s[i +  5]; case  5: d[j +  4] = s[i +  4];
		case  4: d[j +  3] = s[i +  3]; case  3: d[j +  2] = s[i +  2];
		case  2: d[j +  1] = s[i +  1]; case  1: d[j] = s[i]; break; /**!**/
		default: for(int k = n - 1; k >= 0; k--) d[j + k] = s[i + k]; break;
		}
	}
	
	@SuppressWarnings("FallThrough")
	public static final void copy(char[] s, int i, char[] d, int j, int n) {
		
		if (s == d || n > 32) { arraycopy(s, i, d, j, n);} else switch (n) {
		case 16: d[j + 15] = s[i + 15]; case 15: d[j + 14] = s[i + 14];
		case 14: d[j + 13] = s[i + 13]; case 13: d[j + 12] = s[i + 12];
		case 12: d[j + 11] = s[i + 11]; case 11: d[j + 10] = s[i + 10];
		case 10: d[j +  9] = s[i +  9]; case  9: d[j +  8] = s[i +  8];
		case  8: d[j +  7] = s[i +  7]; case  7: d[j +  6] = s[i +  6];
		case  6: d[j +  5] = s[i +  5]; case  5: d[j +  4] = s[i +  4];
		case  4: d[j +  3] = s[i +  3]; case  3: d[j +  2] = s[i +  2];
		case  2: d[j +  1] = s[i +  1]; case  1: d[j] = s[i]; break; /**!**/
		default: for(int k = n - 1; k >= 0; k--) d[j + k] = s[i + k]; break;
		}
	}
	
	@SuppressWarnings("FallThrough")
	public static final void copy(long[] s, int i, long[] d, int j, int n) {
		
		if (s == d || n > 32) { arraycopy(s, i, d, j, n);} else switch (n) {
		case 16: d[j + 15] = s[i + 15]; case 15: d[j + 14] = s[i + 14];
		case 14: d[j + 13] = s[i + 13]; case 13: d[j + 12] = s[i + 12];
		case 12: d[j + 11] = s[i + 11]; case 11: d[j + 10] = s[i + 10];
		case 10: d[j +  9] = s[i +  9]; case  9: d[j +  8] = s[i +  8];
		case  8: d[j +  7] = s[i +  7]; case  7: d[j +  6] = s[i +  6];
		case  6: d[j +  5] = s[i +  5]; case  5: d[j +  4] = s[i +  4];
		case  4: d[j +  3] = s[i +  3]; case  3: d[j +  2] = s[i +  2];
		case  2: d[j +  1] = s[i +  1]; case  1: d[j] = s[i]; break; /**!**/
		default: for(int k = n - 1; k >= 0; k--) d[j + k] = s[i + k]; break;
		}
	}
	
	@SuppressWarnings("FallThrough")
	public static void copy(short[] s , int i , short[] d , int j , int n) {
		
		if (s == d || n > 32) { arraycopy(s, i, d, j, n);} else switch (n) {
		case 16: d[j + 15] = s[i + 15]; case 15: d[j + 14] = s[i + 14];
		case 14: d[j + 13] = s[i + 13]; case 13: d[j + 12] = s[i + 12];
		case 12: d[j + 11] = s[i + 11]; case 11: d[j + 10] = s[i + 10];
		case 10: d[j +  9] = s[i +  9]; case  9: d[j +  8] = s[i +  8];
		case  8: d[j +  7] = s[i +  7]; case  7: d[j +  6] = s[i +  6];
		case  6: d[j +  5] = s[i +  5]; case  5: d[j +  4] = s[i +  4];
		case  4: d[j +  3] = s[i +  3]; case  3: d[j +  2] = s[i +  2];
		case  2: d[j +  1] = s[i +  1]; case  1: d[j] = s[i]; break; /**!**/
		default: for(int k = n - 1; k >= 0; k--) d[j + k] = s[i + k]; break;
		}
	}
	
	@SuppressWarnings("FallThrough")
	public static final <T> void copy( T[] s, int i, T[] d, int j, int n ) {
		
		if (s == d || n > 32) { arraycopy(s, i, d, j, n);} else switch (n) {
		case 16: d[j + 15] = s[i + 15]; case 15: d[j + 14] = s[i + 14];
		case 14: d[j + 13] = s[i + 13]; case 13: d[j + 12] = s[i + 12];
		case 12: d[j + 11] = s[i + 11]; case 11: d[j + 10] = s[i + 10];
		case 10: d[j +  9] = s[i +  9]; case  9: d[j +  8] = s[i +  8];
		case  8: d[j +  7] = s[i +  7]; case  7: d[j +  6] = s[i +  6];
		case  6: d[j +  5] = s[i +  5]; case  5: d[j +  4] = s[i +  4];
		case  4: d[j +  3] = s[i +  3]; case  3: d[j +  2] = s[i +  2];
		case  2: d[j +  1] = s[i +  1]; case  1: d[j] = s[i]; break; /**!**/
		default: for(int k = n - 1; k >= 0; k--) d[j + k] = s[i + k]; break;
		}
	}
	
	/**
	 * 
	 */
	public static int search( final int[] array, int from, int to, int key ) {
		int l = from, h = to - 1; while (l <= h) {
			int m = (l + h) >>> 1; var v = array[m];
			if (v < key) l = m + 1; else if(v > key) h = m - 1; else return m;
		}
		return -(l + 1);
	}
	
	public static int search(final byte[] array, int from, int to, byte key) {
		int l = from, h = to - 1; while (l <= h) {
			int m = (l + h) >>> 1; var v = array[m];
			if (v < key) l = m + 1; else if(v > key) h = m - 1; else return m;
		}
		return -(l + 1);
	}
	
	public static int search(final long[] array, int from, int to, long key) {
		int l = from, h = to - 1; while (l <= h) {
			int m = (l + h) >>> 1; var v = array[m];
			if (v < key) l = m + 1; else if(v > key) h = m - 1; else return m;
		}
		return -(l + 1);
	}
	
	public static int search (short[] array , int from , int to , short key) {
		int l = from, h = to - 1; while (l <= h) {
			int m = (l + h) >>> 1; var v = array[m];
			if (v < key) l = m + 1; else if(v > key) h = m - 1; else return m;
		}
		return -(l + 1);
	}
}
