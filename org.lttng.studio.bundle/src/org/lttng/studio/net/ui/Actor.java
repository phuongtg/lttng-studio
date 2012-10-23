package org.lttng.studio.net.ui;

public class Actor {

	private String label;
	private long id;

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

}
