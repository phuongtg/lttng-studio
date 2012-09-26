package org.lttng.studio.model;

import java.util.Collection;
import java.util.HashMap;

public class SystemModel implements ITraceModel {

	private HashMap<Long, Task> tasks; // (tid, task)
	private long[] current;			// (cpu, tid)
	private int numCpus;

	public SystemModel() {
	}

	public void init(int numCpus) {
		tasks = new HashMap<Long, Task>();
		current = new long[numCpus];
	}

	public Collection<Task> getTasks() {
		return tasks.values();
	}

	public long getCurrentTid(int cpu) {
		return current[cpu];
	}

	public void setCurrentTid(int cpu, long tid) {
		current[cpu] = tid;
	}

	public void putTask(Task task) {
		tasks.put(task.getTid(), task);
	}

	public Task getTask(long tid) {
		return tasks.get(tid);
	}

	public Task getTaskCpu(int cpu) {
		return tasks.get(getCurrentTid(cpu));
	}

	public int getNumCpus() {
		return numCpus;
	}

	@Override
	public void reset() {
		tasks = new HashMap<Long, Task>();
		current = new long[numCpus];
	}
}
