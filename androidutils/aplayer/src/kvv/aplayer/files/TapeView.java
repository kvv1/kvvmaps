package kvv.aplayer.files;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class TapeView extends View {
	private final static int STEP_MS = 30; 

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

	Paint paint = new Paint();
	{
		paint.setAntiAlias(true);
	}
	Paint headboxPaint = new Paint();
	{
		headboxPaint.setColor(0xFF404000);
		headboxPaint.setAntiAlias(true);
	}
	Paint headboxPaint1 = new Paint();
	{
		headboxPaint1.setColor(0xFF000000);
		headboxPaint1.setAntiAlias(true);
		headboxPaint1.setStrokeWidth(3);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRGB(150, 150, 120);

		float w = getWidth();

		float w1 = w;
		if (w > getHeight() * 1.9)
			w1 = (int) (getHeight() * 1.9);

		int drawSize = (int) (w1 / 2.1f);
		float bobbinY = w1 / 4;
		float bobbinX1 = w / 2 - w1 / 4;
		float bobbinX2 = w / 2 + w1 / 4;

		float r1 = bobbin1.getTapeR(drawSize);
		float r2 = bobbin2.getTapeR(drawSize);

		float hbLeft = w / 2 - w1 / 10;
		float hbTop = bobbinY * 1.8f;
		float hbRight = w / 2 + w1 / 10;
		float hbBottom = bobbinY * 2.1f;

		canvas.drawRoundRect(new RectF(hbLeft, hbTop, hbRight, hbBottom), 10,
				10, headboxPaint);

		canvas.drawLine(hbLeft, (hbTop + hbBottom) / 2, hbRight,
				(hbTop + hbBottom) / 2, headboxPaint1);

		float grad = 15;

		float x1 = (float) (bobbinX1 - r1 * Math.sin(Math.toRadians(grad)));
		float y1 = (float) (bobbinY + r1 * Math.cos(Math.toRadians(grad)));
		float x2 = (float) (bobbinX2 + r2 * Math.sin(Math.toRadians(grad)));
		float y2 = (float) (bobbinY + r2 * Math.cos(Math.toRadians(grad)));

		canvas.drawLine(hbLeft, (hbTop + hbBottom) / 2, x1, y1, paint);
		canvas.drawLine(hbRight, (hbTop + hbBottom) / 2, x2, y2, paint);

		bobbin1.draw(canvas, bobbinX1, bobbinY, drawSize);
		bobbin2.draw(canvas, bobbinX2, bobbinY, drawSize);
		/*
		 * float levelX; float levelY;
		 * 
		 * if (w == w1) { levelX = 0; levelY = hbBottom + 10; } else { levelY =
		 * 0; levelX = w1; }
		 * 
		 * int levelSz = w1 / 8;
		 * 
		 * drawLevel(canvas, levelX, levelY, levelSz, levelSz);
		 * drawLevel(canvas, levelX + levelSz + 2, levelY, levelSz, levelSz);
		 */
	}

	public void setProgress(float max, float cur) {
		bobbin1.setPercent(max, max - cur);
		bobbin2.setPercent(max, cur);
	}

	private Runnable r;

	private int seekStep;

	public void start() {
		if (r != null)
			return;

		r = new Runnable() {
			@Override
			public void run() {
				if (seekStep == 0) {
					bobbin1.step(STEP_MS);
					bobbin2.step(STEP_MS);
				} else {
					int step = seekStep / 5;
					if(step > 500)
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
