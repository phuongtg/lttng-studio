package org.lttng.studio.model;

import java.util.HashMap;

public class Task {

	public enum lttng_thread_type {
		LTTNG_USER_THREAD(0),
		LTTNG_KERNEL_THREAD(1);
		private final int val;

		private lttng_thread_type(int val) {
			this.val = val;
		}
		public int value() { return val; }
	}

	public enum lttng_execution_mode {
		LTTNG_USER_MODE(0),
		LTTNG_SYSCALL(1),
		LTTNG_TRAP(2),
		LTTNG_IRQ(3),
		LTTNG_SOFTIRQ(4),
		LTTNG_MODE_UNKNOWN(5);
		private final int val;

		private lttng_execution_mode(int val) {
			this.val = val;
		}
		public int value() { return val; }
	}

	enum lttng_execution_submode {
		LTTNG_NONE(0),
		LTTNG_UNKNOWN(1);
		private final int val;

		private lttng_execution_submode(int val) {
			this.val = val;
		}
		public int value() { return val; }
	}

	enum lttng_process_status {
		LTTNG_UNNAMED(0),
		LTTNG_WAIT_FORK(1),
		LTTNG_WAIT_CPU(2),
		LTTNG_EXIT(3),
		LTTNG_ZOMBIE(4),
		LTTNG_WAIT(5),
		LTTNG_RUN(6),
		LTTNG_DEAD(7);
		private final int val;

		private lttng_process_status(int val) {
			this.val = val;
		}
		public int value() { return val; }
	}

	private long pid;
	private long ppid;
	private long tid;
	private long start;
	private long end;
	private String name;
	private HashMap<Long, String> fdMap;
	private lttng_process_status process_status;
	private lttng_execution_mode execution_mode;
	private lttng_execution_submode execution_submode;
	private lttng_thread_type thread_type;

	public Task() {
		this(0);
		setFdMap(new HashMap<Long, String>());
	}

	public Task(long tid) {
		setTid(tid);
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public HashMap<Long, String> getFdMap() {
		return fdMap;
	}

	public void setFdMap(HashMap<Long, String> fdMap) {
		this.fdMap = fdMap;
	}

	public void addFileDescriptor(long fd, String filename) {
		fdMap.put(fd, filename);
	}

	public void removeFileDescriptor(long fd) {
		fdMap.remove(fd);
	}

	public lttng_process_status getProcess_status() {
		return process_status;
	}

	public void setProcess_status(lttng_process_status process_status) {
		this.process_status = process_status;
	}

	/*
	 * I hate Java: can't just assign the enum from the int value
	 */	
	public void setProcess_status(long status) {
		for (lttng_process_status e: lttng_process_status.values()) {
			if (e.value() == status) {
				process_status = e;
				break;
			}
		}
	}
	
	public lttng_execution_mode getExecution_mode() {
		return execution_mode;
	}

	public void setExecution_mode(long mode) {
		for (lttng_execution_mode e: lttng_execution_mode.values()) {
			if (e.value() == mode) {
				execution_mode = e;
				break;
			}
		}
	}
	
	public void setExecution_mode(lttng_execution_mode execution_mode) {
		this.execution_mode = execution_mode;
	}

	public lttng_execution_submode getExecution_submode() {
		return execution_submode;
	}

	public void setExecution_submode(lttng_execution_submode execution_submode) {
		this.execution_submode = execution_submode;
	}

	public void setExecution_submode(long submode) {
		for (lttng_execution_submode e: lttng_execution_submode.values()) {
			if (e.value() == submode) {
				execution_submode = e;
				break;
			}
		}
	}
	
	public lttng_thread_type getThread_type() {
		return thread_type;
	}

	public void setThread_type(lttng_thread_type thread_type) {
		this.thread_type = thread_type;
	}

	public void setThread_type(long type) {
		for (lttng_thread_type e: lttng_thread_type.values()) {
			if (e.value() == type) {
				thread_type = e;
				break;
			}
		}
	}
	
	public long getPpid() {
		return ppid;
	}

	public void setPpid(long ppid) {
		this.ppid = ppid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return String.format("[%d,%s]", tid, name);
	}
}
