package org.lttng.studio.model;

import java.util.HashMap;

public class SystemModel {

	private HashMap<Long, Task> tasks;
	private HashMap<Long, Integer> cpuTaskMap;
	
	public SystemModel() {
		reset();
	}

	public void reset() {
		setTasks(new HashMap<Long, Task>());
		setCpuTaskMap(new HashMap<Long, Integer>());
	}
	
	public HashMap<Long, Task> getTasks() {
		return tasks;
	}

	public void setTasks(HashMap<Long, Task> tasks) {
		this.tasks = tasks;
	}

	public HashMap<Long, Integer> getCpuTaskMap() {
		return cpuTaskMap;
	}

	public void setCpuTaskMap(HashMap<Long, Integer> cpuTaskMap) {
		this.cpuTaskMap = cpuTaskMap;
	}

	public void putTask(Task task) {
		tasks.put(task.getTid(), task);
	}
	
	public Task getTask(long tid) {
		return tasks.get(tid);
	}

}
