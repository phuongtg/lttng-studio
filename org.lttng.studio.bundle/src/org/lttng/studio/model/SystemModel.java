package org.lttng.studio.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.lttng.studio.reader.TraceReader;

public class SystemModel implements ITraceModel {

	private HashMap<Long, Task> tasks; // (tid, task)
	private HashMap<Long, HashMap<Long, FD>> fds; // (pid, (id, fd))
	private long[] current;			// (cpu, tid)
	private int numCpus;
	private boolean isInitialized = false;

	public SystemModel() {
	}

	@Override
	public void init(TraceReader reader) {
		if (isInitialized == false){
			numCpus = reader.getNumCpus();
			tasks = new HashMap<Long, Task>();
			fds = new HashMap<Long, HashMap<Long, FD>>();
			current = new long[numCpus];
			// Swapper task is always present
			Task swapper = new Task();
			swapper.setName("swapper");
			swapper.setPid(0);
			swapper.setTid(0);
			swapper.setPpid(0);
			tasks.put(0L, swapper);
		}
		isInitialized = true;
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
		long currentTid = getCurrentTid(cpu);
		return tasks.get(currentTid);
	}

	public int getNumCpus() {
		return numCpus;
	}

	@Override
	public void reset() {
		isInitialized = false;
	}

	public void addFD(long pid, FD fd) {
		if (!fds.containsKey(pid)) {
			fds.put(pid, new HashMap<Long, FD>());
		}
		fds.get(pid).put(fd.getNum(), fd);
	}

	public void removeFD(long pid, FD fd) {
		if (fds.containsKey(pid)) {
			HashMap<Long, FD> map = fds.get(pid);
			map.remove(fd.getNum());
		}
	}

	public FD getFD(long pid, long num) {
		if (!fds.containsKey(pid))
			return null;
		return fds.get(pid).get(num);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Tasks\n");
		for (Task task: tasks.values()) {
			str.append(String.format("%10d %10d %10d %s\n", task.getPid(), task.getTid(), task.getPpid(), task.getName()));
			if (fds.containsKey(task.getPid())) {
				for (FD fd: fds.get(task.getPid()).values())
					str.append(String.format("\t%d %s\n", fd.getNum(), fd.getName()));
			}
		}
		return str.toString();
	}

	public Set<FD> getFDs() {
		HashSet<FD> set = new HashSet<FD>();
		for (Long pid: fds.keySet()) {
			set.addAll(fds.get(pid).values());
		}
		return set;
	}

	public void removeFD(long pid, long fd) {
		if (fds.containsKey(pid)) {
			HashMap<Long, FD> map = fds.get(pid);
			map.remove(fd);
		}
	}

}
