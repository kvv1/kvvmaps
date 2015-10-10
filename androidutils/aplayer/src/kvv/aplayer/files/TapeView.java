package kvv.aplayer.files;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

public class TapeView extends View {
	private final static int STEP_MS = 40;

	private Bitmap bmp;
	private int w;
	private int h;

	private float bobbinSize;
	private float bobbinX1;
	private float bobbinX2;
	private float bobbinY;
	private float r1;
	private float r2;

	static class Params {

		protected float bobbinSize;
		protected float bobbinX1;
		protected float bobbinX2;
		protected float bobbinY;
		protected float r1;
		protected float r2;

		public Params(float bobbinSize, float bobbinX1, float bobbinX2,
				float bobbinY, float r1, float r2) {
			super();
			this.bobbinSize = bobbinSize;
			this.bobbinX1 = bobbinX1;
			this.bobbinX2 = bobbinX2;
			this.bobbinY = bobbinY;
			this.r1 = r1;
			this.r2 = r2;
		}

	}

	private void createBmp() {
		if (bmp == null)
			return;

		bobbinSize = bobbinSize(w);
		bobbinY = bobbinY(w);
		bobbinX2 = bobbinX2(w);
		bobbinX1 = bobbinX1(w);
		r1 = bobbin1.getTapeR(bobbinSize);
		r2 = bobbin2.getTapeR(bobbinSize);

		bmp.eraseColor(0);

		Canvas canvas = new Canvas(bmp);

		float hbLeft = w * 0.5f - w * 0.1f;
		float hbTop = h * 0.8f;
		float hbRight = w * 0.5f + w * 0.1f;
		float hbBottom = h * 0.99f;

		float hbGapY = hbTop + (hbBottom - hbTop) * 0.8f;

		canvas.drawRoundRect(new RectF(hbLeft, hbTop, hbRight, hbBottom), 10,
				10, headboxPaint2);
		canvas.drawRoundRect(
				new RectF(hbLeft, hbTop + 4, hbRight, hbBottom - 4), 10, 10,
				headboxPaint);

		canvas.drawLine(hbLeft, hbGapY, hbRight, hbGapY, headboxPaint1);

		float grad = 25;

		float x1 = (float) (bobbinX1 - r1 * Math.sin(Math.toRadians(grad)));
		float y1 = (float) (bobbinY + r1 * Math.cos(Math.toRadians(grad)));
		float x2 = (float) (bobbinX2 + r2 * Math.sin(Math.toRadians(grad)));
		float y2 = (float) (bobbinY + r2 * Math.cos(Math.toRadians(grad)));

		canvas.drawLine(hbLeft, hbGapY, x1, y1, tapePaint);
		canvas.drawLine(hbRight, hbGapY, x2, y2, tapePaint);
	}

	private Handler handler = new Handler();

	public TapeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TapeView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public TapeView(Context context) {
		super(context);
	}

	Bobbin bobbin1 = new Bobbin();
	Bobbin bobbin2 = new Bobbin();
	{
		bobbin1.setPercent(100, 100);
		bobbin2.setPercent(100, 0);
	}

	Paint tapePaint = new Paint();
	{
		tapePaint.setColor(Bobbin.TAPE_COLOR);
		tapePaint.setAntiAlias(true);
	}
	Paint headboxPaint = new Paint();
	{
		headboxPaint.setColor(0xFF505000);
		headboxPaint.setAntiAlias(true);
	}
	Paint headboxPaint2 = new Paint();
	{
		headboxPaint2.setColor(0xFF404000);
		headboxPaint2.setAntiAlias(true);
	}
	Paint headboxPaint1 = new Paint();
	{
		headboxPaint1.setColor(0xFF000000);
		headboxPaint1.setAntiAlias(true);
		headboxPaint1.setStrokeWidth(3);
	}

	private static float bobbinSize(float w) {
		return w * 0.42f;
	}

	private static float bobbinY(float w) {
		return w * 0.22f;
	}

	private static float bobbinX2(float w) {
		return w - w * 0.22f;
	}

	private static float bobbinX1(float w) {
		return w * 0.22f;
	}

	@Override
	public void draw(Canvas canvas) {
		w = getWidth();
		h = getHeight();
		if (h > w * 0.55)
			h = (int) (w * 0.55f);

		if (bmp == null || bmp.getWidth() != w || bmp.getHeight() != h) {
			if (bmp != null) {
				bmp.recycle();
				bmp = null;
			}
			bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			createBmp();
		}

		canvas.drawBitmap(bmp, 0, 0, null);

		bobbin1.draw(canvas, bobbinX1, bobbinY, bobbinSize);
		bobbin2.draw(canvas, bobbinX2, bobbinY, bobbinSize);

		// canvas.drawCircle(w / 10, h * 0.9f, w / 50, tapePaint);
		// canvas.drawCircle(w- w / 10, h * 0.9f, w / 50, tapePaint);

	}

	private boolean hitTest(float x, float y, int bobbin) {
		float w = getWidth();
		float bx = bobbin < 0 ? bobbinX1(w) : bobbinX2(w);
		float by = bobbinY(w);
		return Math.sqrt(((x - bx) * (x - bx) + (y - by) * (y - by))) < bobbinSize(w) * 0.2;
	}

	public int hitTest(float x, float y) { // -1 - left, 1 = right
		if (hitTest(x, y, -1))
			return -1;
		if (hitTest(x, y, 1))
			return 1;

		if (x < w / 5 && y < h / 5)
			return -2;

		if (x > w - w / 5 && y < h / 5)
			return 2;

		return 0;
	}

	public void setProgress(float max, float cur) {
		bobbin1.setPercent(max, max - cur);
		bobbin2.setPercent(max, cur);
		createBmp();
	}

	private Runnable r;

	private int seekStep;

	public void start() {
		if (r != null)
			return;

		r = new Runnable() {
			private long time = SystemClock.uptimeMillis();

			@Override
			public void run() {
				long t = SystemClock.uptimeMillis();
				int dt = (int) (t - time);
				time = t;
				if (seekStep == 0) {
					bobbin1.step(dt);
					bobbin2.step(dt);
				} else {
					int step = seekStep / 5;
					if (step > 500)
						step = 500;
					bobbin1.step(step);
					bobbin2.step(step);
				}

				invalidate();
				handler.postDelayed(r, STEP_MS);
			}
		};

		handler.postDelayed(r, 100);
	}

	public void stop() {
		if (r != null)
			handler.removeCallbacks(r);
		r = null;
	}

	public void setSeek(int step) {
		seekStep = step;
	}
}
