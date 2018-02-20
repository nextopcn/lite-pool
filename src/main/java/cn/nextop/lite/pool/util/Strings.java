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

import cn.nextop.lite.pool.util.reflection.ToStringBuilder;
import cn.nextop.lite.pool.util.reflection.ToStringBuilderEx;

/**
 * @author Jingqi Xu
 */
public final class Strings {

	public static final String EMPTY = "";

	/**
	 * 
	 */
	public static boolean isEmpty(CharSequence c) {
		return c == null || c.length() == 0;
	}

	public static ToStringBuilder build(Object obj) {
		return new ToStringBuilder(obj);
	}

	public static String buildEx(final Object obj) {
		return ToStringBuilderEx.toString(obj);
	}

	public static void delete(StringBuilder builder, int idx, char ch) {
		if (idx >= 0 && builder.charAt(idx) == ch) builder.deleteCharAt(idx);
	}
}
