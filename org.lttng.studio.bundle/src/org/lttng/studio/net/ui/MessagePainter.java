package org.lttng.studio.net.ui;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.lttng.studio.ui.Arrow;

public class MessagePainter {

	HashMap<Actor, ActorLine> actorLineMap;
	Interval window;
	int leftBorder = 400;
	static Color black = new Color(null, 0, 0, 0);
	static Color red = new Color(null, 255, 0, 0);
	static Color blue = new Color(null, 0, 0, 255);

	public MessagePainter() {
		this(new Interval());
	}

	public MessagePainter(Interval window) {
		actorLineMap = new HashMap<Actor, ActorLine>();
		setWindow(window);
	}

	public void paint(GC gc, Image img, Message msg) {
		Rectangle bounds = img.getBounds();
		ActorLine senderLine = actorLineMap.get(msg.sender);
		ActorLine receiverLine = actorLineMap.get(msg.receiver);
		int width = bounds.width - leftBorder;
		int fromX = (int) (width * (msg.sent - window.getStart()) / window.getDuration()) + leftBorder;
		int fromY = senderLine.getPos();
		int destX = (int) (width * (msg.recv - window.getStart()) / window.getDuration()) + leftBorder;
		int destY = receiverLine.getPos();

		gc.setForeground(black);
		gc.setBackground(black);
		gc.drawLine(fromX, fromY, destX, destY);
		Arrow.drawArrow(destX, fromX, destY, fromY, gc);
	}

	public void updateActorLines(Actor[] actors, int height, int border) {
		int i = 0;
		int spacing = 0;
		actorLineMap.clear();
		if (actors.length > 1)
			spacing = (height - (border * 2)) / (actors.length - 1);
		for (Actor actor : actors) {
			ActorLine line = new ActorLine(actor.getInterval(), window);
			line.setPos(spacing * i + border);
			line.setLeftBorder(leftBorder);
			line.setActor(actor);
			actorLineMap.put(actor, line);
			i++;
		}
	}

	public void paintActorLines(GC gc, Image img) {
		for (ActorLine line: actorLineMap.values()) {
			line.paint(gc, img);
		}
	}

	public Interval getWindow() {
		return window;
	}

	public void setWindow(Interval window) {
		this.window = window;
	}
}