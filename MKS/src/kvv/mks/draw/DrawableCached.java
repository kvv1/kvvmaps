package kvv.mks.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class DrawableCached implements Drawable {

	private BufferedImage im;
	private boolean on;
	
	public abstract void _draw(Graphics2D g, int w, int h);

	public void invalidateCache() {
		im = null;
	}
	

	public void cacheOn() {
		on = true;
	}

	public void cacheOff() {
		on = false;
		invalidateCache();
	}

	@Override
	public final void draw(Graphics2D g, int w, int h) {
		if(!on) {
			_draw(g, w, h);
			return;
		}
		
		if (im == null || im.getWidth() != w || im.getHeight() != h) {
			im = null;

			im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g1 = (Graphics2D) im.getGraphics();
			g1.setColor(new Color(0, 0, 0, 0));
			g1.fillRect(0, 0, w, h);
			_draw(g1, w, h);
		}
		g.drawImage(im, 0, 0, null);
	}

}
