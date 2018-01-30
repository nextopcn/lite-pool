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

package cn.nextop.lite.pool.util.scheduler.impl.executor;

import cn.nextop.lite.pool.util.Concurrents;
import cn.nextop.lite.pool.util.concurrent.thread.XThreadFactory;
import cn.nextop.lite.pool.util.scheduler.Job;
import cn.nextop.lite.pool.util.scheduler.impl.AbstractScheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * @author Jingqi Xu
 */
public class ExecutorScheduler extends AbstractScheduler<ExecutorTrigger> {
	//
	private final int corePoolSize;
	private final AtomicReference<ThreadFactory> factory;
	private final AtomicReference<ScheduledThreadPoolExecutor> executor;
	
	/**
	 * 
	 */
	public ExecutorScheduler(String name, int corePoolSize) {
		this(name, corePoolSize, new XThreadFactory(name));
	}
	
	public ExecutorScheduler(String name, int corePoolSize, ThreadFactory tf) {
		super(name);
		this.factory = new AtomicReference<ThreadFactory>(tf);
		this.verbose = false; this.corePoolSize = corePoolSize;
		this.executor = new AtomicReference<ScheduledThreadPoolExecutor>();
	}
	
	protected void doStart() throws Exception {
		// 1
		super.doStart();
		
		// 2
		this.factory.compareAndSet(null, new XThreadFactory(name, false));
		ScheduledThreadPoolExecutor s = new ScheduledThreadPoolExecutor(this.corePoolSize);
		s.setRemoveOnCancelPolicy(true); s.setThreadFactory(factory.get()); executor.set(s);
	}
	
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		// 2
		timeout = Concurrents.terminate(this.executor.getAndSet(null), timeout, unit);
		
		// 1
		return super.doStop(timeout, unit);
	}
	
	/**
	 * 
	 */
	@Override
	protected void doUnschedule(RunnableJob rj) throws Exception {
		((ScheduledFuture<?>)rj.getCookie()).cancel(true);
	}
	
	@Override
	protected void doSchedule(final RunnableJob rj) throws Exception {
		Job<ExecutorTrigger> job = rj.getJob();
		final ExecutorTrigger t = job.getTrigger();
		final long delay = t.getDelay(), interval = t.getInterval();
		if(interval <= 0) {
			rj.setCookie(this.executor.get().schedule(rj, delay, t.getTimeUnit()));
		} else {
			final TimeUnit unit = t.getTimeUnit();
			if(t.isFixedRate()) {
				rj.setCookie(executor.get().scheduleAtFixedRate(rj, delay, interval, unit));
			} else {
				rj.setCookie(executor.get().scheduleWithFixedDelay(rj, delay, interval, unit));
			}
		}
	}
}
