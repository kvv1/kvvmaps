package kvv.aplayer.files.tape;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.smartbean.androidutils.util.Utils;

public class ArrowLevelView extends LevelView {

	private Paint arrowPaint = new Paint();
	{
		arrowPaint.setAntiAlias(true);
		arrowPaint.setStrokeWidth(2);
		arrowPaint.setColor(0xFF802020);
	}

	private int[] scale;

	public ArrowLevelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ArrowLevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ArrowLevelView(Context context) {
		super(context);
	}

	@Override
	public void setScale(int[] scale) {
		this.scale = scale;
		createBitmap();
	}

	private Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

	static int bmpSize = 256;
	static final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
	static final Bitmap bmpScale = Bitmap.createBitmap(bmpSize, bmpSize, conf);
	static final Bitmap bmpBody = Bitmap.createBitmap(bmpSize, bmpSize, conf);

	private float cx2(float w) {
		return w / 2;
	}

	private float cy2(float w) {
		return w * 6 / 7;
	}

	private float cx3(float w, float r, float val) {
		if (val > 110)
			val = 110;
		double angle = Math.toRadians((val - 50) * 35 / 50);
		return (float) (cx2(w) + r * Math.sin(angle));
	}

	private float cy3(float w, float r, float val) {
		if (val > 110)
			val = 110;
		double angle = Math.toRadians((val - 50) * 35 / 50);
		return (float) (cy2(w) - r * Math.cos(angle));
	}

	{
		createBitmap();
	}

	void createBitmap() {
		int c0 = 0xFF404000;
		// int c0 = 0xFF606000;
		// int c1 = 0xFF556B2F;
		int c1 = 0xFF505000;
		int c2 = 0xFFFFA500;

		float w = bmpBody.getWidth();

		float cx1 = w / 2;
		float cy1 = w / 2;

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		Canvas scaleCanvas = new Canvas(bmpScale);

		paint.setColor(c2);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		scaleCanvas.drawCircle(cx1, cy1, w * 0.44f, paint);

		float r1 = w * 0.50f;
		float r2 = w * 0.55f;

		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(4);

		if (scale != null)
			for (int db : scale) {
				float val = (float) (Utils.db2n(db) * 66);
				// float val = (float) (Math.pow(10, db / 20f) * 66);

				float x1 = cx3(w, r1, val);
				float y1 = cy3(w, r1, val);
				float x2 = cx3(w, r2, val);
				float y2 = cy3(w, r2, val);

				scaleCanvas.drawLine(x1, y1, x2, y2, paint);

				paint.setTextSize(10);
				scaleCanvas.drawText("" + (int) db, cx3(w, r2 * 1.1f, val) - 6,
						cy3(w, r2 * 1.1f, val) + 6, paint);

			}

		scaleCanvas.drawArc(new RectF(cx2(w) - r1, cy2(w) - r1, cx2(w) + r1,
				cy2(w) + r1), -125, 70, false, paint);

		paint.setTextSize(20);
		scaleCanvas.drawText("dB", cx3(w, r2 * 0.6f, 50) - 13,
				cy3(w, r2 * 0.6f, 50), paint);

		Canvas bodyCanvas = new Canvas(bmpBody);

		paint.setStyle(Paint.Style.STROKE);

		paint.setStrokeWidth(w / 20);
		paint.setColor(c0);
		bodyCanvas.drawCircle(cx1, cy1, w * 0.44f, paint);

		paint.setStrokeWidth(w / 15);
		paint.setColor(c1);
		bodyCanvas.drawCircle(cx1, cy1, w * 0.41f, paint);

		paint.setStyle(Paint.Style.FILL);

		paint.setColor(c1);
		bodyCanvas.drawArc(new RectF(cx2(w) - w / 4, cy2(w) - w / 4, cx2(w) + w
				/ 4, cy2(w) + w / 4), -180, 180, true, paint);

		paint.setColor(c1);
		bodyCanvas.drawArc(new RectF(cx1 - w * 0.44f, cy1 - w * 0.44f, cx1 + w
				* 0.44f, cy1 + w * 0.44f), 30, 120, false, paint);

		paint.setColor(Color.BLACK);
		bodyCanvas.drawCircle(cx2(w), cy2(w), w / 20, paint);
	}

	@Override
	public void draw(Canvas canvas) {
		paint.setAntiAlias(true);

		float w = getWidth();

		canvas.drawBitmap(bmpScale, null, new RectF(0, 0, w, w), paint);

		float val = (float) (lpf.get() * 100 / levelLpf.get());

		// val = 0;
		float r = w * 0.6f;

		canvas.drawLine(cx2(w), cy2(w), cx3(w, r, val), cy3(w, r, val),
				arrowPaint);

		canvas.drawBitmap(bmpBody, null, new RectF(0, 0, w, w), paint);

	}

	public void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
		int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		setMeasuredDimension(size, size);
	}
}
