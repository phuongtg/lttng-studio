package org.lttng.studio.model;

public class EventCounter implements ITraceModel {

	private long counter;

	public EventCounter() {
		setCounter(0);
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

	public void increment() {
		counter++;
	}

	@Override
	public void reset() {
		counter = 0;
	}

}
