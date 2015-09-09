package kvv.aplayer.files;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class Bobbin {

	static final int tapeMinR = 30;
	static final int tapeMaxR = 115;

	private float max;
	private float cur;
	private float angle;

	static final int bmSize = 256;

	static final int bgColor = 0xFF808080;
	// static final int bgColor = 0;
	static final int color = 0xFFE0F0E0;
	static final int color1 = 0xFFB0C0B0;

	static final Paint bobbinPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	static final Paint tapePaint = new Paint();
	static final Paint bgPaint = new Paint();
	static final Paint axisPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	//private static final int TAPE_COLOR = 0xFF8b4513;
	public static final int TAPE_COLOR = 0xFF401004;
	
	static {
		bobbinPaint.setAlpha(200);
		bgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		bgPaint.setColor(bgColor);
		bgPaint.setAntiAlias(true);
		tapePaint.setColor(TAPE_COLOR);
		tapePaint.setAntiAlias(true);
		axisPaint.setAntiAlias(true);
	}

	static final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
	static final Bitmap bmp = Bitmap.createBitmap(bmSize, bmSize, conf);
	static final Bitmap bmpAxis = Bitmap.createBitmap(20, 20, conf);

	static {
		bmp.eraseColor(0);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		float cx = canvas.getWidth() / 2;
		float cy = canvas.getHeight() / 2;

		canvas.translate(cx, cy);

		// ///////////////////

		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color1);
		paint.setStrokeWidth(22);
		canvas.drawCircle(0, 0, 117, paint);

		// ///////////////////

		Bitmap bmp1 = Bitmap.createBitmap(bmSize, bmSize, conf);
		bmp1.eraseColor(0);
		Canvas canvas1 = new Canvas(bmp1);
		canvas1.translate(cx, cy);

		Path path = new Path();
		path.moveTo(-20, 10);
		path.lineTo(-10, 117);
		path.lineTo(10, 117);
		path.lineTo(20, 10);

		Paint paint1 = new Paint();
		paint1.setAntiAlias(true);

		paint1.setStyle(Paint.Style.STROKE);
		paint1.setColor(color1);
		paint1.setStrokeWidth(4);
		canvas1.drawPath(path, paint1);

		paint1.setStyle(Paint.Style.FILL);
		paint1.setColor(color);
		canvas1.drawPath(path, paint1);

		canvas.drawBitmap(bmp1, -cx, -cy, paint);
		canvas.rotate(120);
		canvas.drawBitmap(bmp1, -cx, -cy, paint);
		canvas.rotate(120);
		canvas.drawBitmap(bmp1, -cx, -cy, paint);

		// ///////////////////

		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(17);
		canvas.drawCircle(0, 0, 117, paint);

		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas.drawCircle(0, 0, 30, paint);

		paint.setColor(0xFF404040);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		canvas.drawArc(new RectF(-5, -35, 5, -25), 0, 180, false, paint);
		
		
		
		Canvas c = new Canvas(bmpAxis);
		c.translate(bmpAxis.getWidth() / 2, bmpAxis.getHeight() / 2);
		Paint p = new Paint();
		p.setAntiAlias(true);
		c.drawCircle(0, 0, bmpAxis.getWidth() / 3, p);
		p.setStrokeWidth(4);
		c.drawLine(0, 0, 0, bmpAxis.getHeight() / 2, p);
		c.rotate(120);
		c.drawLine(0, 0, 0, bmpAxis.getHeight() / 2, p);
		c.rotate(120);
		c.drawLine(0, 0, 0, bmpAxis.getHeight() / 2, p);
	}

	private void drawAxis(Canvas canvas, float cx, float cy, float drawSize) {
		canvas.save();
		canvas.translate(cx, cy);
		canvas.rotate(angle);
		float w = drawSize / 20;
		canvas.drawBitmap(bmpAxis,
				new Rect(0, 0, bmpAxis.getWidth(), bmpAxis.getHeight()),
				new RectF(-w, -w, w, w), axisPaint);
		canvas.restore();
	}

	private float getTapeR(float r1, float r2, float max, float cur) {
		return (float) Math.sqrt((cur - max) / max * (r2 * r2 - r1 * r1) + r2
				* r2);
	}

	private void drawTapeCircle(Canvas canvas, float cx, float cy, float drawSize) {
		float r1 = getTapeR(tapeMinR, tapeMaxR, max, cur) * drawSize / bmSize;

		float r0 = tapeMinR * drawSize / bmSize;

		tapePaint.setStyle(Paint.Style.STROKE);
		tapePaint.setStrokeWidth(r1 - r0);
		canvas.drawCircle(cx, cy, (r1 + r0) / 2, tapePaint);

		// canvas.drawCircle(cx, cy, r1, tapePaint);
		// canvas.drawCircle(cx, cy, tapeMinR * drawSize / bmSize, bgPaint);
	}

	private void drawBobbin(Canvas canvas, float cx, float cy, float drawSize) {
		canvas.save();
		canvas.translate(cx, cy);
		canvas.rotate(angle);
		canvas.drawBitmap(bmp, new Rect(0, 0, bmSize, bmSize), new RectF(
				-drawSize / 2, -drawSize / 2, drawSize / 2, drawSize / 2),
				bobbinPaint);
		canvas.restore();
	}

	public void draw(Canvas canvas, float x, float y, float drawSize) {
		drawTapeCircle(canvas, x, y, drawSize);
		drawBobbin(canvas, x, y, drawSize);
		drawAxis(canvas, x, y, drawSize);
	}

	public void step(int ms) {
		float r1 = getTapeR(tapeMinR * 100 / tapeMaxR, 100, max, cur);
		float da = 5 * ms / r1;

		this.angle -= da;
		if (angle < -360)
			angle += 360;
	}

	public void setPercent(float max, float cur) {
		this.max = max;
		this.cur = cur;
	}

	public float getTapeR(float drawSize) {
		return getTapeR(tapeMinR, tapeMaxR, max, cur) * drawSize / bmSize;
	}

}
