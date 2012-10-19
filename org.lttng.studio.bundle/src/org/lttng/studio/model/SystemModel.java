package org.lttng.studio.model;

import java.util.Collection;
import java.util.HashMap;

import org.lttng.studio.model.task.Task;
import org.lttng.studio.reader.TraceReader;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;

public class SystemModel implements ITraceModel {

	private HashMap<Long, Task> tasks; // (tid, task)
	private Table<Long, Long, FD> fdsTable; // (pid, id, fd)
	private Table<Long, Long, InetSock> socksTable; // (pid, sk, sock)
	private BiMap<Long, Long> sockFd; // (sk, fd)
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
			fdsTable =  HashBasedTable.create();
			socksTable = HashBasedTable.create();
			sockFd = HashBiMap.create();
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

	/*
	 * FIXME: there seems between FD and Sock
	 * Opportunity to simplify into object with some id per pid
	 */

	/*
	 * FDs management
	 */
	public void addFD(long pid, FD fd) {
		fdsTable.put(pid, fd.getNum(), fd);
	}

	public void removeFD(long pid, long fdNum) {
		fdsTable.remove(pid, fdNum);
	}

	public FD getFD(long pid, long num) {
		return fdsTable.get(pid, num);
	}

	public Collection<FD> getFDs() {
		return fdsTable.values();
	}

	public void dup2FD(long pid, long oldfd, long newfd) {
		// dup2 does nothing if oldfd == newfd
		if (oldfd == newfd)
			return;
		// Copy oldfd, assign newfd
		FD ofd = getFD(pid, oldfd);
		String name = null;
		if (ofd == null) {
			System.err.println("WARNING: dup2 of unkown fd");
		} else {
			name = ofd.getName();
		}
		FD nfd = new FD(newfd, name);
		removeFD(pid, oldfd);
		addFD(pid, nfd);

		// manage sock relationship if any

	}

	/*
	 * Socks management
	 */
	public void addInetSock(long pid, InetSock sock) {
		socksTable.put(pid, sock.getSk(), sock);
	}

	public void removeInetSock(long pid, long sk) {
		socksTable.remove(pid, sk);
	}

	public InetSock getInetSock(long pid, long sk) {
		return socksTable.get(pid, sk);
	}

	public Collection<InetSock> getInetSocks() {
		return socksTable.values();
	}

	public void setInetSockFd(long sock, long fd) {
		// FIXME: this is shitty because BiMap can't have duplicated value
		// and FDs numbers are not unique on the system, but sk pointer is
		sockFd.forcePut(sock, fd);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Tasks\n");
		for (Task task: tasks.values()) {
			str.append(String.format("%10d %10d %10d %s\n", task.getPid(), task.getTid(), task.getPpid(), task.getName()));
			for (Long fdNum: fdsTable.rowKeySet()) {
				FD fd = fdsTable.get(task.getPid(), fdNum);
				str.append(String.format("\t%d %s\n", fd.getNum(), fd.getName()));
			}
		}
		return str.toString();
	}

}
