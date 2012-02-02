package kvv.kvvmap.adapter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;

public class GC {
	public final Canvas canvas;
	private final Paint paint;
	private final int width;
	private final int height;

	private float strokeWidth;
	private int color;
	private Style style;
	private float textSize;
	private int flags;
	private Arrow arrow;

	public GC(Canvas canvas, Paint paint, int w, int h) {
		this.canvas = canvas;
		this.paint = paint;
		this.width = w;
		this.height = h;

		int sz = width / 16;
		if (sz > 32)
			sz = 32;
		arrow = new Arrow(sz);
	}

	private DashPathEffect pathEffect = new DashPathEffect(new float[] { 15, 5,
			8, 5 }, 0);

	public void setDashed(boolean b) {
		if (b)
			paint.setPathEffect(pathEffect);
		else
			paint.setPathEffect(null);
	}

	/*
	 * public void setClip(RegionInt reg) { Path path = new Path(); for
	 * (PointInt pt : reg.points) { if (pt == reg.points.get(0))
	 * path.moveTo(pt.x, pt.y); else path.lineTo(pt.x, pt.y); } path.close();
	 * 
	 * canvas.clipPath(path, Region.Op.REPLACE); }
	 */
	public void storeDrawingParams() {
		strokeWidth = paint.getStrokeWidth();
		color = paint.getColor();
		style = paint.getStyle();
		textSize = paint.getTextSize();
		flags = paint.getFlags();
	}

	public void restoreDrawingParams() {
		paint.setStrokeWidth(strokeWidth);
		paint.setColor(color);
		paint.setStyle(style);
		paint.setTextSize(textSize);
		paint.setFlags(flags);
	}

	public void setAntiAlias(boolean b) {
		paint.setAntiAlias(b);
	}

	public void setStrokeWidth(int w) {
		paint.setStrokeWidth(w);
	}

	public void drawImage(Object img, int x, int y) {
		paint.setFilterBitmap(true);
		canvas.drawBitmap((Bitmap) img, x, y, paint);
	}

	public void drawImage(Object img, RectInt src, RectInt dst) {
		paint.setFilterBitmap(true);
		Bitmap bm = (Bitmap) img;
		canvas.drawBitmap(bm, src.rect, dst.rect, paint);
	}

	private final Rect src = new Rect();
	private final Rect dst = new Rect();

	public void drawImage(Object img, int x, int y, int factor) {
		paint.setFilterBitmap(true);
		synchronized (src) {
			Bitmap bm = (Bitmap) img;
			src.set(0, 0, bm.getWidth(), bm.getHeight());
			dst.set(x, y, x + bm.getWidth() * factor, y + bm.getHeight()
					* factor);
			canvas.drawBitmap(bm, src, dst, paint);
		}
	}

	// public void drawImage(Object img, int dstx, int dsty, int srcx, int srcy,
	// int w, int h) {
	// canvas.drawBitmap((Bitmap) img, new Rect(srcx, srcy, srcx + w, srcy + h),
	// new Rect(dstx, dsty, dstx+256)
	// }

	// public void drawImage(Object img, ITransform transform) {
	// canvas.drawBitmap((Bitmap) img,
	// ((Transform1) transform).affineTransform, paint);
	// }

	public void fillRect(float l, float t, float r, float b) {
		paint.setStyle(Style.FILL);
		canvas.drawRect(l, t, r, b, paint);
	}

	public void drawRect(float l, float t, float r, float b) {
		paint.setStyle(Style.STROKE);
		canvas.drawRect(l, t, r, b, paint);
	}

	public RectX getTextBounds(String text) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		return new RectX(bounds);
	}

	public void drawText(String text, int x, int y) {
		canvas.drawText(text, x, y, paint);
	}

	public void setColor(int c) {
		paint.setColor(c);
	}

	public void setTextSize(int sz) {
		paint.setTextSize(sz);
	}

	public void drawLine(float x1, float y1, float x2, float y2) {
		canvas.drawLine(x1, y1, x2, y2, paint);
	}

	public void drawCircle(float x, float y, float r) {
		paint.setStyle(Style.STROKE);
		canvas.drawCircle(x, y, r, paint);
	}

	public void fillCircle(float x, float y, float r) {
		paint.setStyle(Style.FILL);
		canvas.drawCircle(x, y, r, paint);
	}

	public int getColor() {
		return paint.getColor();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void clearClip() {
		canvas.clipRect(0, 0, width, height, Region.Op.REPLACE);
	}

	public void setDstOverMode(boolean b) {
		if (b)
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
		else
			paint.setXfermode(null);
	}

	public void drawArrow(int x, int y, float rot, boolean dimmed) {
		arrow.draw(canvas, x, y, rot, dimmed);
	}

	Matrix m;

	public void setTransform(float[] trans) {
		m = canvas.getMatrix();
		Matrix m = new Matrix();
		m.setPolyToPoly(trans, 0, trans, trans.length / 2, trans.length / 4);
		canvas.setMatrix(m);
	}

	public void setTransform(float deg, float px, float py) {
		m = canvas.getMatrix();
		Matrix m1 = new Matrix(m);
		m1.postRotate(deg, px, py);
		canvas.setMatrix(m1);
	}

	public void clearTransform() {
		canvas.setMatrix(m);
	}

}
