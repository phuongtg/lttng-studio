package org.lttng.studio.net.ui;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ActorLine {

	private int pos;
	private int leftBorder;
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
		int width = bounds.width - leftBorder;
		long t1 = life.getStart();
		long t2 = life.getEnd();
		if (t1 < window.getStart())
			t1 = window.getStart();
		if (t2 > window.getEnd())
			t2 = window.getEnd();
		int x1 = (int) (width * (t1 - window.getStart()) / window.getDuration());
		int x2 = (int) (width * (t2 - window.getStart()) / window.getDuration());
		gc.drawLine(x1 + leftBorder, pos, x2 + leftBorder, pos);
		Point size = gc.textExtent(actor.getLabel());
		gc.drawText(actor.getLabel(), size.y, pos - size.y);
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

	public int getLeftBorder() {
		return leftBorder;
	}

	public void setLeftBorder(int border) {
		this.leftBorder = border;
	}
}
