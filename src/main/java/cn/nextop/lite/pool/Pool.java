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


import cn.nextop.lite.pool.glossary.Named;

import java.util.concurrent.TimeUnit;

/**
 * @author Baoyi Chen
 */
public interface Pool<T> extends Named {
	
	/**
	 * 
	 */
	PoolConfig<T> getConfig();
	
	T acquire(); void release(T t);
	
	T acquire(long timeout, TimeUnit unit);
	
	/**
	 * 
	 */
	void publish(PoolEvent<T> event);
	
	boolean addListener(PoolListener<T> listener);
	
	boolean delListener(PoolListener<T> listener);
}
