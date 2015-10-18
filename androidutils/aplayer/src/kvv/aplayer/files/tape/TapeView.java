package kvv.aplayer.files.tape;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint("NewApi")
public class TapeView extends View {
	private final static int STEP_MS = 20;

	private static final int TAPE_COLOR = 0xFF401004;

	private int w;
	private int h;

	private float bobbinSize;
	private float bobbinX1;
	private float bobbinX2;
	private float bobbinY;
	private float r1;
	private float r2;

	private BobbinView b1;
	private BobbinView b2;

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

	public TapeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public TapeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TapeView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
	}

	Paint tapePaint = new Paint();
	{
		tapePaint.setColor(TAPE_COLOR);
		tapePaint.setAntiAlias(true);
		tapePaint.setStyle(Paint.Style.STROKE);
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

	private float max = 100;

	private float cur = 40;

	{
		headboxPaint1.setColor(0xFF000000);
		headboxPaint1.setAntiAlias(true);
		headboxPaint1.setStrokeWidth(3);
	}

	@Override
	public void draw(Canvas canvas) {

		if (b1 != null) {
			bobbinSize = b1.getWidth();
			bobbinX1 = b1.getX() + bobbinSize / 2;
			bobbinX2 = b2.getX() + bobbinSize / 2;
			bobbinY = b1.getY() + bobbinSize / 2;
		}

		w = getWidth();
		h = getHeight();
		if (h > w * 0.55)
			h = (int) (w * 0.55f);

		r1 = getTapeR(max - cur);
		r2 = getTapeR(cur);

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

		tapePaint.setStrokeWidth(1);
		canvas.drawLine(hbLeft, hbGapY, x1, y1, tapePaint);
		canvas.drawLine(hbRight, hbGapY, x2, y2, tapePaint);

		drawTapeCircle(canvas, bobbinX1, max - cur);
		drawTapeCircle(canvas, bobbinX2, cur);

	}

	private void drawTapeCircle(Canvas canvas, float bobbinX, float cur) {
		float r1 = getTapeR(BobbinBmp.tapeMinR, BobbinBmp.tapeMaxR, max, cur)
				* bobbinSize / BobbinBmp.bmSize;
		float r0 = BobbinBmp.tapeMinR * bobbinSize / BobbinBmp.bmSize;
		tapePaint.setStrokeWidth(r1 - r0);
		canvas.drawCircle(bobbinX, bobbinY, (r1 + r0) / 2, tapePaint);
	}

	private float getTapeR(float r1, float r2, float max, float cur) {
		return (float) Math.sqrt((cur - max) / max * (r2 * r2 - r1 * r1) + r2
				* r2);
	}

	public float getTapeR(float cur) {
		return getTapeR(BobbinBmp.tapeMinR, BobbinBmp.tapeMaxR, max, cur)
				* bobbinSize / BobbinBmp.bmSize;
	}

	public void setProgress(float max, float cur) {
		this.max = max;
		this.cur = cur;
		invalidate();
	}

	private Runnable r;
	private Handler handler = new Handler();

	private int seekStep;

	boolean started;

	public void start() {
		if (started || b1 == null)
			return;

		started = true;

		b1.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		b2.setLayerType(View.LAYER_TYPE_HARDWARE, null);

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
					int step = STEP_MS;
					step = (dt + STEP_MS)/2;
					step(b1, step);
					step(b2, step);
				} else {
					int step = seekStep / 5;
					if (step > 500)
						step = 500;
					step(b1, step);
					step(b2, step);
				}

				invalidate();
				handler.postDelayed(r, STEP_MS);
			}
		};

		handler.postDelayed(r, 100);
	}

	void step(BobbinView bobbin, int ms) {
		float r1 = getTapeR(BobbinBmp.tapeMinR * 100 / BobbinBmp.tapeMaxR, 100,
				max, bobbin == b1 ? (max - cur) : cur);
		float da = 5 * ms / r1;

		float angle = bobbin.getRotation();
		angle -= da;

		if (angle < -360)
			angle += 360;
		if (angle > 360)
			angle -= 360;

		//bobbin.animate().rotation(angle).start();
		
		bobbin.setRotation(angle);
	}

	public void stop() {
		if (!started || b1 == null)
			return;
		started = false;

		if (r != null)
			handler.removeCallbacks(r);
		r = null;

	}

	public void setSeek(int step) {
		seekStep = step;
	}

	public void setBobbins(BobbinView b1, BobbinView b2) {
		this.b1 = b1;
		this.b2 = b2;
	}
}