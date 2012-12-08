package ants;

import java.util.Set;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

public class Ant extends Obj {

	AntSkin skin = new AntSkin();

	private int step = 2;

	private static final double dist = Ants.CELL_SIZE;

	public Ant(int offsetX, int offsetY, RGB rgb) {
		super(offsetX, offsetY);
		skin.color = new Color(Ants.display, rgb);
	}

	public void draw(GC gc) {
		skin.draw(gc);
	}

	public void step() {

		if (brick != null) {

		} else {

			double dst = Math.random() * dist;

			for (int cx = cellx - 1; cx <= cellx + 1; cx++)
				for (int cy = celly - 1; cy <= celly + 1; cy++) {
					if (cx < 0 || cx >= Ants.pf.cw || cy < 0
							|| cy >= Ants.pf.ch)
						continue;
					Set<Obj> cell = Ants.pf.cells[cx][cy];
					if(cell == null)
						continue;
					synchronized (cell) {
						for (Obj o : Ants.pf.cells[cx][cy]) {
							if (!tooFar(o) && o instanceof Ant && o != this) {
								Ant ant = (Ant) o;

								double r = distTo(ant);

								if (r < 16) {
									rotate(Math
											.toRadians(Math.random() * 90 - 45));
								} else if (r < dst) {
									double a = angleTo(ant);
									if (Math.abs(a) < Math.toRadians(70)) {
										if (a > 0)
											rotate(Math.toRadians(2));
										if (a < 0)
											rotate(Math.toRadians(-2));
									}
								}
							}
						}
					}
				}
		}

		borders();

		skin.step();

		rotate(Math.toRadians(Math.random() * 10 - 5));
		forward(step);
	}

	private boolean tooFar(Obj o) {
		return Math.abs(x - o.x) > dist || Math.abs(y - o.y) > dist;
	}

	private void borders() {
		if (x < 0 && Math.abs(angle) > Math.PI / 2) {
			if (angle > 0)
				rotate(-Math.PI / 2);
			else
				rotate(Math.PI / 2);
		} else if (x >= Ants.pf.w && Math.abs(angle) < Math.PI / 2) {
			if (angle > 0)
				rotate(Math.PI / 2);
			else
				rotate(-Math.PI / 2);
		}

		if (y < 0 && angle < 0) {
			if (angle < -Math.PI / 2)
				rotate(-Math.PI / 2);
			else
				rotate(Math.PI / 2);
		} else if (y >= Ants.pf.h && angle > 0) {
			if (angle < Math.PI / 2)
				rotate(-Math.PI / 2);
			else
				rotate(Math.PI / 2);
		}
	}

	Brick brick;

	void get(Brick br) {
		brick = br;
		br.ant = this;
	}

	void free() {
		if (brick != null) {
			brick.ant = null;
			brick = null;
		}
	}
}
