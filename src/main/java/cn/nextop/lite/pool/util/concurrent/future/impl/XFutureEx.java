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

package cn.nextop.lite.pool.util.concurrent.future.impl;

import cn.nextop.lite.pool.util.concurrent.future.FutureEx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Jingqi Xu
 */
@SuppressWarnings("unchecked")
public class XFutureEx extends AbstractFuture<Void> implements FutureEx<Void> {
	//
	protected volatile Object cookie;
	protected volatile Throwable throwable;
	protected volatile Listener<Void> listener;
	
	/**
	 * 
	 */
	public XFutureEx() {
	}
	
	public XFutureEx(boolean completed) {
		if(completed) complete();
	}
	
	public XFutureEx(Throwable throwable) {
		complete(throwable);
	}
	
	/**
	 * 
	 */
	@Override
	public <V> V getCookie() {
		return (V)cookie;
	}
	
	@Override
	public FutureEx<Void> setCookie(Object cookie) {
		this.cookie = cookie; return this;
	}
	
	/**
	 * 
	 */
	public synchronized void complete() {
		this.sync.releaseShared(1);
		Listener<Void> r = this.listener; if(r != null) r.onComplete(this);
	}
	
	public synchronized void complete(Throwable throwable) {
		this.throwable = throwable; this.sync.releaseShared(1);
		Listener<Void> r = this.listener; if(r != null) r.onComplete(this);
	}
	
	@Override
	public synchronized Listener<Void> setListener(Listener<Void> listener) {
		Listener<Void> r = this.listener; this.listener = listener;
		if(this.isDone() && listener != null) listener.onComplete(this); return r;
	}
	
	/**
	 * 
	 */
	@Override
	public Void get() throws InterruptedException, ExecutionException {
		//
		this.sync.acquireSharedInterruptibly(1);
		
		//
		if(throwable == null) { return null; } else { throw new ExecutionException(throwable); }
	}
	
	@Override
	public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		//
		timeout = unit.toNanos(timeout);
		if(!this.sync.tryAcquireSharedNanos(1, timeout)) {
			timeout = unit.convert(timeout, TimeUnit.NANOSECONDS);
			throw new TimeoutException("timeout: " + timeout + ", unit: " + unit);
		}
		
		//
		if(throwable == null) { return null; } else { throw new ExecutionException(throwable); }
	}
}
