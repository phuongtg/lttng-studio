package org.lttng.studio.tests.sock;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDrawSock {

	static int imgWidth = 800;
	static int imgHeight = 200;
	static int border = 40;
	static Color black = new Color(null, 0, 0, 0);
	static Color red = new Color(null, 255, 0, 0);
	static Color blue = new Color(null, 0, 0, 255);
	Image img;
	GC gc;
	String testName;

	public class Message {
		long sent;
		long recv;
		Actor sender;
		Actor receiver;

		public Message(Actor sender, long sent, Actor receiver, long recv) {
			this.sent = sent;
			this.recv = recv;
			this.sender = sender;
			this.receiver = receiver;
		}
	}

	public static class Actor {
		String name;
		static int count = 0;
		int id;

		public Actor(String name) {
			this.name = name;
			this.id = count++;
		}
	}

	public class ActorLine {
		int y;
		String label;

		public ActorLine(int height, String label) {
			this.y = height;
			this.label = label;
		}
	}

	@Before
	public void setup() {
		img = new Image(null, imgWidth, imgHeight);
		gc = new GC(img);
	}

	@After
	public void teardown() {
		img.dispose();
		gc.dispose();
	}

	public void saveImage(Image img, String name) {
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { img.getImageData() };
		loader.save(name + ".png", SWT.IMAGE_PNG);
	}

	public int timeToPix(long end, int width, long time) {
		return (int) (time * width / end);
	}

	@Test
	public void testDrawSock() {
		Actor[] actors = new Actor[] { new Actor("client"), new Actor("server") };
		Actor client = actors[0];
		Actor server = actors[1];

		HashMap<Actor, ActorLine> actorLineMap = new HashMap<Actor, ActorLine>();

		Message[] msgs = new Message[] { new Message(client, 10L, server, 20L),
				new Message(server, 30L, client, 40L), };

		int i = 1;
		for (Actor actor : actors) {
			ActorLine line = new ActorLine(border * i++, actor.name);
			actorLineMap.put(actor, line);
			gc.drawLine(0, line.y, imgWidth, line.y);

		}

		for (Message msg : msgs) {
			ActorLine senderLine = actorLineMap.get(msg.sender);
			ActorLine receiverLine = actorLineMap.get(msg.receiver);
			Point from = new Point(timeToPix(50L, imgWidth, msg.sent),
					senderLine.y);
			Point dest = new Point(timeToPix(50L, imgWidth, msg.recv),
					receiverLine.y);
			gc.setForeground(black);
			gc.setBackground(red);
			gc.drawLine(from.x, from.y, dest.x, dest.y);
			drawArrow(dest.x, from.x, dest.y, from.y, gc);
			System.out.println(from + " " + dest);
		}

		saveImage(img, "test_draw_sock");
	}

	@Test
	public void testDrawArrow() {

		int numArrow = 10;
		Point tail = new Point(border, imgHeight - border);
		int y2 = border;
		int width = imgWidth - border;
		gc.setBackground(red);
		gc.setForeground(red);
		for (int x = 0; x < numArrow; x++) {
			int x2 = (x * width / numArrow) + border;
			gc.drawLine(tail.x, tail.y, x2, y2);
			drawArrow(x2, tail.x, y2, tail.y, gc);
		}
		saveImage(img, "test_draw_arrow");
	}

	/*
	 * Source:
	 * http://stackoverflow.com/questions/3010803/draw-arrow-on-line-algorithm
	 */
	private void drawArrow(int tipX, int tailX, int tipY, int tailY, GC gc) {
		int arrowLength = 15;
		int angle = 20;
		int dx = tipX - tailX;
		int dy = tipY - tailY;

		double theta = Math.atan2(dy, dx);

		double rad = Math.toRadians(angle);
		long x = Math.round(tipX - arrowLength * Math.cos(theta + rad));
		long y = Math.round(tipY - arrowLength * Math.sin(theta + rad));

		double phi2 = Math.toRadians(-angle);
		long x2 = Math.round(tipX - arrowLength * Math.cos(theta + phi2));
		long y2 = Math.round(tipY - arrowLength * Math.sin(theta + phi2));

		int[] arrow = new int[] { tipX, tipY, (int) x, (int) y, (int) x2,
				(int) y2 };
		gc.fillPolygon(arrow);
	}

}
