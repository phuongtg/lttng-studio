package org.lttng.studio.net.ui;

public class Actor {

	private String label;
	private long id;
	private long start;
	private long end;

	public Actor(String label, long id) {
		setLabel(label);
		setId(id);
	}

	public Actor() {
		this("", 0);
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return this.label;
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

	public void setInterval(Interval interval) {
		this.start = interval.getStart();
		this.end = interval.getEnd();
	}

	public Interval getInterval() {
		return new Interval(start, end);
	}

}
