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

package cn.nextop.lite.pool.util.scheduler;

import cn.nextop.lite.pool.glossary.Lifecycle;

import java.util.List;
import java.util.Set;

/**
 * @author Jingqi Xu
 */
public interface Scheduler<T> extends Lifecycle {
	
	/**
	 * 
	 */
	void pause();
	
	void resume();
	
	Set<Job<T>> getAllJobs();
	
	void schedule(Job<T> job);
	
	Job<T> getJob(String jobId);
	
	boolean pause(String jobId);
	
	boolean resume(String jobId);
	
	boolean isPaused(String jobId);
	
	boolean unschedule(String jobId);
	
	/**
	 * 
	 */
	List<SchedulingListener> getSchedulingListeners();
	
	boolean addSchedulingListener(SchedulingListener listener);
	
	boolean delSchedulingListener(SchedulingListener listener);
}
