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
 * 
 * @author Jingqi Xu
 */
public class AssertionException extends RuntimeException {
	//
	private static final long serialVersionUID = 7066468784796847964L;
	
	/**
	 * 
	 */
	public AssertionException() {
		this(null, null);
	}
	
	public AssertionException(String msg) {
		this(msg, null);
	}
	
	public AssertionException(Throwable cause) {
		this(null, cause);
	}
	
	public AssertionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
