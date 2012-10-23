package org.lttng.studio.tests.sock;

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
import org.lttng.studio.net.ui.Actor;
import org.lttng.studio.net.ui.Interval;
import org.lttng.studio.net.ui.Message;
import org.lttng.studio.net.ui.MessagePainter;
import org.lttng.studio.ui.Arrow;

public class TestDrawSock {

	static int imgWidth = 800;
	static int imgHeight = 200;
	static int border = 40;
	Image img;
	GC gc;
	String testName;

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

	@Test
	public void testDrawSock() {
		Actor[] actors = new Actor[] { new Actor("client", 0), new Actor("server", 1) };
		Actor client = actors[0];
		Actor server = actors[1];

		Message[] msgs = new Message[] {
				new Message(client, 10L, server, 20L),
				new Message(server, 30L, client, 40L),
				};

		Interval window = Message.getWindow(msgs);

		MessagePainter painter = new MessagePainter(window);
		painter.updateActorLines(actors, img.getBounds().height, border);
		painter.paintActorLines(gc, img);
		for (Message msg : msgs) {
			painter.paint(gc, img, msg);
		}
		saveImage(img, "test_draw_sock");
	}

	@Test
	public void testDrawArrow() {
		int numArrow = 10;
		Point tail = new Point(border, imgHeight - border);
		int y2 = border;
		int width = imgWidth - border;
		Color red = new Color(null, 255, 0, 0);
		gc.setBackground(red);
		gc.setForeground(red);
		for (int x = 0; x < numArrow; x++) {
			int x2 = (x * width / numArrow) + border;
			gc.drawLine(tail.x, tail.y, x2, y2);
			Arrow.drawArrow(x2, tail.x, y2, tail.y, gc);
		}
		saveImage(img, "test_draw_arrow");
	}

}
