package org.lttng.studio.net.ui;

public class Interval {

	private long start;
	private long end;

	public Interval(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public Interval() {
		this(0, 1);
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
	public long getDuration() {
		return this.end - this.start;
	}
	@Override
	public String toString() {
		return "{" + start + "," + end + "}";
	}

}
