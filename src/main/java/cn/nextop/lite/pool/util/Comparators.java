/*
 * Copyright 2016-2018 Nextop Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.lite.pool.util;

/**
 * @author Jingqi Xu
 */
public final class Comparators {
	
	/**
	 * 
	 */
	public static int cmp(int v1, int v2, boolean asc) {
		int r = 0; if(v1 < v2) r = -1; else if(v1 > v2) r = 1; return asc ? r : -r;
	}
	
	public static int cmp(long v1, long v2, boolean asc) {
		int r = 0; if(v1 < v2) r = -1; else if(v1 > v2) r = 1; return asc ? r : -r;
	}
	
	public static int cmp(short v1, short v2, boolean asc) {
		int r = 0; if(v1 < v2) r = -1; else if(v1 > v2) r = 1; return asc ? r : -r;
	}

	public static final <T extends Comparable<? super T>> int cmp(T v1, T v2, boolean asc) {
		return asc ? v1.compareTo(v2) : -(v1.compareTo(v2));
	}
	
	public static final <T extends Comparable<? super T>> int cmp(T v1, T v2, boolean asc, boolean nullIsGreater) {
		int r = 0; if (v1 == null && v2 == null) r = 0; else if(v1 == null) r = nullIsGreater ? 1 : -1;
		else if(v2 == null) r = nullIsGreater ? -1 : 1; else r = v1.compareTo(v2); return asc ? r : -r;
	}
}
