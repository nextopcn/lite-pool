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

import cn.nextop.lite.pool.util.Strings;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author Jingqi Xu
 */
public class ExecutorTrigger implements Serializable {
	//
	private static final long serialVersionUID = -3026337937891537082L;
	
	//
	protected long delay;
	protected long interval;
	protected TimeUnit timeUnit;
	protected boolean fixedRate;
	
	/**
	 * 
	 */
	public ExecutorTrigger() {
	}
	
	public ExecutorTrigger(long delay, long interval, TimeUnit timeUnit, boolean fixRate) {
		this.delay = delay;
		this.interval = interval;
		this.timeUnit = timeUnit;
		this.fixedRate = fixRate;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return Strings.build(this)
		.append("delay", delay)
		.append("interval", interval)
		.append("timeUnit", timeUnit)
		.append("fixedRate", fixedRate).toString();
	}
	
	/**
	 * 
	 */
	public long getDelay() {
		return delay;
	}
	
	public void setDelay(long delay) {
		this.delay = delay;
	}
	
	public long getInterval() {
		return interval;
	}
	
	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	public boolean isFixedRate() {
		return fixedRate;
	}
	
	public void setFixedRate(boolean fixedRate) {
		this.fixedRate = fixedRate;
	}
	
	/**
	 * 
	 */
	public static ExecutorTrigger delay(long delay, TimeUnit timeUnit) {
		return new ExecutorTrigger(delay, 0, timeUnit, false);
	}
	
	public static ExecutorTrigger fixRate(long delay, long interval, TimeUnit timeUnit) {
		return new ExecutorTrigger(delay, interval, timeUnit, true);
	}
	
	public static ExecutorTrigger fixDelay(long delay, long interval, TimeUnit timeUnit) {
		return new ExecutorTrigger(delay, interval, timeUnit, false);
	}
}
