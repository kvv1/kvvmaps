package ants;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

public abstract class Obj {

	private Transform trans = new Transform(Ants.display);

	float elems[] = new float[6];
	{
		trans.getElements(elems);
	}

	int cellx;
	int celly;

	double x;
	double y;

	double angle;

	public Obj(int offsetX, int offsetY) {
		x = offsetX;
		y = offsetY;
	}
	public abstract void draw(GC gc);
	public abstract void step();

	final void rotate(double angle) {
		this.angle += angle;
		this.angle = normAngle(this.angle);
	}

	final void forward(int n) {
		x += n * Math.cos(angle);
		y += n * Math.sin(angle);
		Ants.pf.moveObj(this);
	}

	final double angleTo(double _x, double _y) {
		double dx = _x - x;
		double dy = _y - y;
		double atan2 = Math.atan2(dy, dx);
		double a = atan2 - angle;
		return normAngle(a);
	}

	final double angleTo(Obj obj) {
		return angleTo(obj.x, obj.y);
	}

	final double distTo(double _x, double _y){
		double dx = _x - x;
		double dy = _y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	final double distTo(Obj obj) {
		return distTo(obj.x, obj.y);
	}

	static double normAngle(double a){
		while(a > Math.PI)
			a -= 2 * Math.PI;
		while(a < -Math.PI)
			a += 2 * Math.PI;
		return a;
	}
	
	final public void _draw(GC gc) {
		trans.setElements(elems[0], elems[1], elems[2], elems[3], elems[4],
				elems[5]);
		trans.translate((int) x, (int) y);
		trans.rotate((float) Math.toDegrees(angle));

		gc.setTransform(trans);

		draw(gc);
		

		gc.setTransform(null);
	}
}
