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

package cn.nextop.lite.pool.util.scheduler.impl;

import cn.nextop.lite.pool.glossary.Lifecyclet;
import cn.nextop.lite.pool.util.Objects;
import cn.nextop.lite.pool.util.Strings;
import cn.nextop.lite.pool.util.scheduler.Job;
import cn.nextop.lite.pool.util.scheduler.Scheduler;
import cn.nextop.lite.pool.util.scheduler.SchedulingException;
import cn.nextop.lite.pool.util.scheduler.SchedulingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Jingqi Xu
 */
public abstract class AbstractScheduler<T> extends Lifecyclet implements Scheduler<T> {
	//
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduler.class);
	
	//
	protected final String name;
	protected final ConcurrentHashMap<String, RunnableJob> jobs;
	protected final CopyOnWriteArrayList<SchedulingListener> listeners;
	
	//
	protected abstract void doSchedule(RunnableJob job) throws Exception;
	protected abstract void doUnschedule(RunnableJob job) throws Exception;
	
	/**
	 * 
	 */
	public AbstractScheduler(String name) {
		this.name = name;
		this.jobs = new ConcurrentHashMap<String, RunnableJob>();
		this.listeners = new CopyOnWriteArrayList<SchedulingListener>();
	}
	
	@Override
	protected void doStart() throws Exception {
	}

	@Override
	protected long doStop(long timeout, TimeUnit unit) throws Exception {
		return timeout;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return Strings.build(this)
		.append("name", name).toString();
	}
	
	@Override
	public boolean pause(String id) {
		final RunnableJob job = this.jobs.get(id);
		return job == null ? false : job.pause();
	}
	
	@Override
	public boolean resume(String id) {
		final RunnableJob job = this.jobs.get(id);
		return job == null ? false : job.resume();
	}
	
	@Override
	public boolean isPaused(String id) {
		final RunnableJob job = this.jobs.get(id);
		return job == null ? false : job.isPaused();
	}
	
	@Override
	public void pause() {
		for(RunnableJob rj : jobs.values()) rj.pause();
	}
	
	@Override
	public void resume() {
		for(RunnableJob rj : jobs.values()) rj.resume();
	}
	
	/**
	 * 
	 */
	@Override
	public final void schedule(Job<T> job) {
		try {
			doSchedule(create(job));
		} catch(SchedulingException e) {
			this.jobs.remove(job.getId());
			throw e;
		} catch(Exception e) {
			this.jobs.remove(job.getId());
			throw new SchedulingException(e);
		} finally {
			this.notifyOnSchedule(job);
		}
	}
	
	@Override
	public boolean unschedule(final String id) {
		RunnableJob job = this.jobs.remove(id);
		if(job == null) return false;
		try {
			doUnschedule(job); return true;
		} catch(SchedulingException e) {
			throw e;
		} catch(Exception e) {
			throw new SchedulingException(e);
		} finally {
			this.notifyOnUnschedule(job.getJob());
		}
	}
	
	@Override
	public final Job<T> getJob(final String id) {
		final RunnableJob rj = jobs.get(id);
		return rj == null ? null : rj.getJob();
	}
	
	@Override
	public final Set<Job<T>> getAllJobs() {
		final Set<Job<T>> r = new HashSet<>(jobs.size());
		for(RunnableJob rj : jobs.values()) r.add(rj.getJob());
		return r;
	}
	
	@Override
	public final List<SchedulingListener> getSchedulingListeners() {
		return new ArrayList<SchedulingListener>(this.listeners);
	}
	
	@Override
	public final boolean addSchedulingListener(SchedulingListener listener) {
		return this.listeners.add(listener);
	}
	
	@Override
	public final boolean delSchedulingListener(SchedulingListener listener) {
		return this.listeners.remove(listener);
	}
	
	/**
	 * 
	 */
	protected RunnableJob create(Job<T> job) {
		final RunnableJob rj = new RunnableJob(job);
		if(this.jobs.putIfAbsent(job.getId(), rj) != null) {
			throw new AlreadyScheduledException(job.getId());
		}
		return rj;
	}
	
	protected void notifyOnSchedule(Job<T> job) {
		for(SchedulingListener listener : this.listeners) {
			try {
				listener.onSchedule(job);
			} catch(Exception e) {
				LOGGER.error("unhandled exception in scheduling listener", e);
			}
		}
	}
	
	protected void notifyOnUnschedule(Job<T> job) {
		for(SchedulingListener listener : this.listeners) {
			try {
				listener.onUnschedule(job);
			} catch(Exception e) {
				LOGGER.error("unhandled exception in scheduling listener", e);
			}
		}
	}
	
	protected void notifyMissFire(Job<T> job) {
		for(SchedulingListener listener : this.listeners) {
			try {
				listener.missFire(job);
			} catch(Exception e) {
				LOGGER.error("unhandled exception in scheduling listener", e);
			}
		}
	}
	
	protected void notifyPreFire(Job<T> job) {
		for(SchedulingListener listener : this.listeners) {
			try {
				listener.preFire(job);
			} catch(Exception e) {
				LOGGER.error("unhandled exception in scheduling listener", e);
			}
		}
	}
	
	protected void notifyPostFire(Job<T> job, Throwable uncaught) {
		for(SchedulingListener listener : this.listeners) {
			try {
				listener.postFire(job, uncaught);
			} catch(Exception e) {
				LOGGER.error("unhandled exception in scheduling listener", e);
			}
		}
	}
	
	/**
	 * 
	 */
	protected class RunnableJob implements Runnable {
		//
		protected final Job<T> job;
		protected volatile Object cookie;
		protected final AtomicLong count = new AtomicLong(0);
		protected final AtomicLong timestamp = new AtomicLong(0);
		protected final AtomicBoolean paused = new AtomicBoolean(false);
		
		//
		public Job<T> getJob() { return job; }
		public boolean isPaused() { return paused.get(); }
		public RunnableJob(Job<T> job) { this.job = job; }
		public <V> V getCookie() { return Objects.cast(cookie); }
		public void setCookie(Object cookie) { this.cookie = cookie; }
		public boolean pause() { return paused.compareAndSet(false, true); }
		public boolean resume() { return paused.compareAndSet(true, false); }
		
		/**
		 * 
		 */
		public void run() {
			//
			if(isPaused()) {
				notifyMissFire(this.job); return;
			}
			
			//
			Throwable throwable = null;
			try {
				notifyPreFire(this.job);
				this.job.getTask().run();
				this.count.incrementAndGet();
				this.timestamp.set(System.currentTimeMillis());
			} catch(Throwable e) {
				throwable = e;
			} finally {
				notifyPostFire(this.job, throwable);
			}
		}
	}
}
