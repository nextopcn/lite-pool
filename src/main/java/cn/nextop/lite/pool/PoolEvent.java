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

package cn.nextop.lite.pool;

import cn.nextop.lite.pool.util.Strings;

/**
 * 
 * @author Jingqi Xu
 */
public class PoolEvent<T> {
	//
	public enum Type { ACQUIRE, RELEASE, LEAKAGE }
	
	//
	private T item;
	private Type type;
	
	/**
	 * 
	 */
	public T getItem() {
		return item;
	}
	
	public void setItem(T item) {
		this.item = item;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return Strings.build(this)
		.append("type", this.type)
		.append("item", item).toString();
	}
	
	/**
	 * 
	 */
	public static final <T> PoolEvent<T> acquire(final T item) {
		PoolEvent<T> r = new PoolEvent<>(); r.type = Type.ACQUIRE; r.item = item; return r;
	}
	
	public static final <T> PoolEvent<T> release(final T item) {
		PoolEvent<T> r = new PoolEvent<>(); r.type = Type.RELEASE; r.item = item; return r;
	}
	
	public static final <T> PoolEvent<T> leakage(final T item) {
		PoolEvent<T> r = new PoolEvent<>(); r.type = Type.LEAKAGE; r.item = item; return r;
	}
}
