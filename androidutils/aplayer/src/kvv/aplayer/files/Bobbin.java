package kvv.aplayer.files;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;

public class Bobbin {

	private float max;
	private float cur;
	private float angle;

	static final Paint bobbinPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	static final Paint tapePaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	public static final int TAPE_COLOR = 0xFF401004;

	{
		tapePaint.setColor(TAPE_COLOR);
		tapePaint.setAntiAlias(true);
		tapePaint.setStyle(Paint.Style.STROKE);
	}

	private float getTapeR(float r1, float r2, float max, float cur) {
		return (float) Math.sqrt((cur - max) / max * (r2 * r2 - r1 * r1) + r2
				* r2);
	}

	private void drawTapeCircle(Canvas canvas, float cx, float cy,
			float drawSize) {
		float r1 = getTapeR(BobbinBmp.tapeMinR, BobbinBmp.tapeMaxR, max, cur)
				* drawSize / BobbinBmp.bmSize;
		float r0 = BobbinBmp.tapeMinR * drawSize / BobbinBmp.bmSize;
		tapePaint.setStrokeWidth(r1 - r0);
		canvas.drawCircle(cx, cy, (r1 + r0) / 2, tapePaint);
	}

	private void drawBobbin(Canvas canvas, float cx, float cy, float drawSize) {
		canvas.save();
		canvas.translate(cx, cy);
		canvas.rotate(angle);
		Bitmap bitmap = BobbinBmp.getInstance();
		canvas.drawBitmap(bitmap,
				new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
				new RectF(-drawSize / 2, -drawSize / 2, drawSize / 2,
						drawSize / 2), bobbinPaint);
		canvas.restore();
	}

	public void draw(Canvas canvas, float x, float y, float drawSize) {
		long t = SystemClock.uptimeMillis();
		drawTapeCircle(canvas, x, y, drawSize);
		long t1 = SystemClock.uptimeMillis();
		drawBobbin(canvas, x, y, drawSize);
		long t2 = SystemClock.uptimeMillis();

		System.out.println((t2 - t1) + " " + (t1 - t));
	}

	public void step(int ms) {
		float r1 = getTapeR(BobbinBmp.tapeMinR * 100 / BobbinBmp.tapeMaxR, 100,
				max, cur);
		float da = 5 * ms / r1;

		this.angle -= da;
		if (angle < -360)
			angle += 360;
		if (angle > 360)
			angle -= 360;
	}

	public void setPercent(float max, float cur) {
		this.max = max;
		this.cur = cur;
	}

	public float getTapeR(float drawSize) {
		return getTapeR(BobbinBmp.tapeMinR, BobbinBmp.tapeMaxR, max, cur)
				* drawSize / BobbinBmp.bmSize;
	}

}
