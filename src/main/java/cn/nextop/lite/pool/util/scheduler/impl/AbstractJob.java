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

import cn.nextop.lite.pool.util.Strings;
import cn.nextop.lite.pool.util.scheduler.Job;

/**
 * @author Jingqi Xu
 */
public class AbstractJob<T> implements Job<T> {
	//
	private String id;
	private T trigger;
	private Runnable task;
	
	/**
	 * 
	 */
	public AbstractJob() {
	}
	
	public AbstractJob(String id, Runnable task, T trigger) {
		this.id = id;
		this.task = task;
		this.trigger = trigger;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return Strings.build(this)
		.append("id", id)
		.append("task", task)
		.append("trigger", trigger).toString();
	}
	
	/**
	 * 
	 */
	@Override
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public T getTrigger() {
		return trigger;
	}
	
	public void setTrigger(T trigger) {
		this.trigger = trigger;
	}
	
	@Override
	public Runnable getTask() {
		return task;
	}
	
	public void setTask(Runnable task) {
		this.task = task;
	}
}
