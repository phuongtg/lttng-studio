package org.lttng.studio.ui;

import org.eclipse.swt.graphics.GC;

public class Arrow {
	/*
	 * Source:
	 * http://stackoverflow.com/questions/3010803/draw-arrow-on-line-algorithm
	 */
	public static void drawArrow(int tipX, int tailX, int tipY, int tailY, GC gc) {
		int arrowLength = 12;
		int angle = 20;
		int dx = tipX - tailX;
		int dy = tipY - tailY;

		int posx = tailX + dx / 2;
		int posy = tailY + dy / 2;

		double theta = Math.atan2(dy, dx);

		double rad = Math.toRadians(angle);
		long x = Math.round(posx - arrowLength * Math.cos(theta + rad));
		long y = Math.round(posy - arrowLength * Math.sin(theta + rad));

		double phi2 = Math.toRadians(-angle);
		long x2 = Math.round(posx - arrowLength * Math.cos(theta + phi2));
		long y2 = Math.round(posy - arrowLength * Math.sin(theta + phi2));

		int[] arrow = new int[] { posx, posy, (int) x, (int) y, (int) x2,
				(int) y2 };
		gc.fillPolygon(arrow);
	}
}
