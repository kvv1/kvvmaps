package ants;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class Brick extends Obj {

	public Brick(int offsetX, int offsetY) {
		super(offsetX, offsetY);
	}

	Color color = new Color(Ants.display, 128, 128, 128);
	public Ant ant;
	
	@Override
	public void draw(GC gc) {
		Color c = gc.getBackground();
		gc.setBackground(color);
		gc.fillArc(-4, -4, 8, 8, 0, 360);
		gc.setBackground(c);
	}

	@Override
	public void step() {
	}

}
