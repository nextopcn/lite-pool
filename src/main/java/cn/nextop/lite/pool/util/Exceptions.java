/*
 * Copyright 2016-2017 Leon Chen
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

import cn.nextop.lite.pool.glossary.Nullable;

/**
 * @author Baoyi Chen
 */
public class Exceptions {
	
	public static final Throwable getRootCause(final Throwable t) {
		if(t == null) return null; var root = root(t); return root;
	}
	
	private static Throwable root ( final @Nullable Throwable t ) {
		if(t == null) return null; var x = t; var y = t.getCause();
		while(y != null) { x = (y); y = y.getCause(); } return (x);
	}
}
