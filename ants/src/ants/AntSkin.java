/**
 * 
 */
package ants;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;

class AntSkin {
	private int d = 0;

	private static final int D = 4;

	Color color;

	private static final Image im;
	static {
		ImageData data = new ImageData(AntSkin.class.getResourceAsStream("ant.png"));
		data.transparentPixel = data.palette.getPixel(new RGB(255,255,255));
		im = new Image(Ants.display, data);
	}

	static Transform tr = new Transform(Ants.display);
	static {
		tr.translate(10, 10);
	}

	static Color bgColor = new Color(Ants.display, 255, 255, 255);

	public void draw(GC gc) {

		if (im == null) {
//			im = new Image(Ants.display, "aa.gif");
//			im.setBackground(bgColor);
/*			
			im = new Image(Ants.display, 20, 20);
			GC gc1 = new GC(im);
			im.setBackground(bgColor);
			gc1.setBackground(bgColor);
			gc1.fillRectangle(0, 0, im.getBounds().width, im.getBounds().height);
			gc1.setBackground(color);
			gc1.setTransform(tr);
			gc1.fillArc(-10, -3, 10, 6, 0, 360);
			gc1.fillArc(0, -3, 5, 6, 0, 360);
			gc1.fillArc(5, -3, 5, 6, 0, 360);
			gc1.dispose();
*/
		}
//		im.setBackground(bgColor);

		
// gc.fillArc(-10, -3, 10, 6, 0, 360);
// gc.fillArc(0, -3, 5, 6, 0, 360);
// gc.fillArc(5, -3, 5, 6, 0, 360);
 
//		gc.drawImage(im, -10, -10);
		 Color c = gc.getBackground();
		 gc.setBackground(color);
		 gc.fillArc(-10, -3, 20, 6, 0, 360); 
		 gc.setBackground(c);
		
		/*
		 * 
		 * int dd;
		 * 
		 * dd = d % D; gc.drawLine(1, 0, -3 + D / 2 - dd, -8); dd = (d + 1) % D;
		 * gc.drawLine(1, 0, -3 + D / 2 - dd, 8);
		 * 
		 * dd = (d + 2) % D; gc.drawLine(2, 0, 2 + D / 2 - dd, -8); dd = (d + 3) %
		 * D; gc.drawLine(2, 0, 2 + D / 2 - dd, 8);
		 * 
		 * dd = (d + 4) % D; gc.drawLine(3, 0, 7 + D / 2 - dd, -8); dd = (d + 5) %
		 * D; gc.drawLine(3, 0, 7 + D / 2 - dd, 8);
		 */
	}

	void step() {
		d++;
	}
}