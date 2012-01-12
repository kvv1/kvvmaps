package kvv.kvvmap.adapter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

public class GC {
	public final Graphics2D g;
	private final int width;
	private final int height;

	public GC(Graphics2D g, int width, int height) {
		this.g = g;
		this.width = width;
		this.height = height;
		font = g.getFont();
		// g.fillRect(0, 0, width, height);
		g.clipRect(0, 0, width, height);
	}

	private Stroke stroke;
	private Color color;
	private Font font;

	public void storeDrawingParams() {
		stroke = g.getStroke();
		color = g.getColor();
		font = g.getFont();
	}

	public void restoreDrawingParams() {
		g.setStroke(stroke);
		g.setColor(color);
		g.setFont(font);
	}

	public void setAntiAlias(boolean b) {
	}

	public void setStrokeWidth(int w) {
		g.setStroke(new BasicStroke(w));
	}

	public void fillRect(float l, float t, float r, float b) {
		g.fillRect((int) l, (int) t, (int) (r - l), (int) (b - t));
	}

	public void drawRect(float l, float t, float r, float b) {
		g.drawRect((int) l, (int) t, (int) (r - l), (int) (b - t));
	}

	public RectX getTextBounds(String text) {
		return new RectX(g.getFontMetrics().getStringBounds(text, g));
	}

	public void drawText(String text, int x, int y) {
		g.drawString(text, x, y);
	}

	public void setColor(int c) {
		g.setColor(new Color(c, true));
	}

	public void setTextSize(int sz) {
		g.setFont(new Font(font.getName(), Font.PLAIN, sz));
	}

	public void drawLine(float x1, float y1, float x2, float y2) {
		g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
	}

	public void drawCircle(float x, float y, float r) {
		g.drawOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
	}

	public void fillCircle(float x, float y, float r) {
		g.fillOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
	}

	public int getColor() {
		return g.getColor().getRGB();
	}

	public void drawImage(Object img, int x, int y) {
		g.drawImage((BufferedImage) img, x, y, null);
	}

	public void drawImage(Object img, int x, int y, int factor) {
		BufferedImage bm = (BufferedImage) img;
		g.drawImage(bm, x, y, bm.getWidth() * factor, bm.getHeight() * factor,
				null);
	}

	// public void drawImage(Object img, int dstx, int dsty, int srcx, int srcy,
	// int w, int h) {
	// g.drawImage((BufferedImage) img, dstx, dsty, dstx + w, dsty + h, srcx,
	// srcy, srcx + w, srcy + h, null);
	// }

	public void clipRect(int x, int y, int w, int h) {
		g.clipRect(x, y, w, h);
	}

	public void drawText(String str, float x, float y) {
		g.drawString(str, x, y);
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void clearClip() {
		g.setClip(null);
	}

	public void setDstOverMode(boolean b) {
		if (b)
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER));
		else
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));

	}

	public void drawArrow(int x, int y, LocationX myLocation, boolean dimmed) {
	}

}
