package org.lttng.studio.net.ui;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class ActorLine {

	private int pos;
	private Interval life;
	private Interval window;
	private Actor actor;

	public ActorLine(Interval life, Interval window) {
		setLife(life);
		setWindow(window);
	}

	public ActorLine() {
		this(new Interval(), new Interval());
	}
	public void paint(GC gc, Image img) {
		Rectangle bounds = img.getBounds();
		int x1 = (int) (bounds.width * (life.getStart() - window.getStart()) / window.getDuration());
		int x2 = (int) (bounds.width * (life.getEnd() - window.getStart()) / window.getDuration());
		gc.drawLine(x1, pos, x2, pos);
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}

	public Interval getLife() {
		return life;
	}

	public void setLife(Interval life) {
		this.life = life;
	}

	public Interval getWindow() {
		return window;
	}

	public void setWindow(Interval window) {
		this.window = window;
	}

	public Actor getActor() {
		return actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
	}
}
