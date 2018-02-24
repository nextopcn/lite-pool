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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.identityHashCode;

/**
 * @author Jingqi Xu
 */
@SuppressWarnings({"unchecked"})
public final class Objects {

	/**
	 * 
	 */
	public static <T> T cast(Object obj) {
		return (T)obj;
	}

	public static List<Class<?>> getSuperClasses(Class<?> clazz) {
		final List<Class<?>> r = new ArrayList<>();
		do r.add(clazz); while ((clazz = clazz.getSuperclass()) != null); return r;
	}

	public static void identityToString(StringBuilder b, Object obj) {
		java.util.Objects.requireNonNull(obj); String name = obj.getClass().getName();
		b.append(name).append('@').append(Integer.toHexString(identityHashCode(obj)));
	}

	public static <T> T getFieldValue(Field f, Object o) {
		try { return cast(f.get(o)); } catch (Throwable e) { throw new RuntimeException(e); }
	}
}
