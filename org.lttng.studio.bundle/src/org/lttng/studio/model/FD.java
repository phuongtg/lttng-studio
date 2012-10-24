package org.lttng.studio.model;

public class FD {

	private long num;
	private String name;

	public FD(long num, String name) {
		setNum(num);
		setName(name);
	}

	public FD(long num) {
		this(num, null);
	}

	public long getNum() {
		return num;
	}

	public void setNum(long num) {
		this.num = num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
