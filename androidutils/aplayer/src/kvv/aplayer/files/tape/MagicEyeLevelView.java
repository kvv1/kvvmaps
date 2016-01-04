package kvv.aplayer.files.tape;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

public class MagicEyeLevelView extends LevelView {

	public MagicEyeLevelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MagicEyeLevelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MagicEyeLevelView(Context context) {
		super(context);
	}

	int bgEdge = 30;
	int edge = 25;

	int W = 50;
	int H = 75;

	private Bitmap bmBg = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);

	private Bitmap bm = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
	private Canvas canvas2 = new Canvas(bm);
	private RectF rectOval = new RectF(0, H - W, W, H - 1);

	private Paint bgPaint = new Paint();
	private Paint sectorPaint = new Paint();
	private Paint sectorPaint1 = new Paint();

	
	{
		bgPaint.setColor(0xFF509050);

		sectorPaint.setColor(0xFF00CC00);
		sectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		sectorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		sectorPaint.setAntiAlias(true);

		sectorPaint1.setColor(0xFF00FF00);
		sectorPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
		sectorPaint1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		sectorPaint1.setAntiAlias(true);

		//lpf.set(0.01);
		//lpf.set(1.1);

		int w = bmBg.getWidth();
		int h = bmBg.getHeight();
		Canvas bgCanvas = new Canvas(bmBg);
		Paint bgPaint1 = new Paint();
		bgPaint1.setColor(0xFF404040);
		
		bgPaint1.setAntiAlias(true);
		bgPaint.setAntiAlias(true);

		bgCanvas.drawRect(0, 0, w, h - w / 2, bgPaint1);
		bgCanvas.drawOval(rectOval, bgPaint1);
	}

	private Path path = new Path();

	private Rect srcRect = new Rect(0, 0, 0, 0);
	private Rect dstRect = new Rect(0, 0, 0, 0);

	@Override
	protected void onDraw(Canvas canvas) {
		bm.eraseColor(0);

		canvas2.drawRect(0, 0, W, H - W / 2, bgPaint);
		canvas2.drawOval(rectOval, bgPaint);

		double val = lpf.get() / levelLpf.get();

		double angle = val * Math.PI / 4;
		if (angle < Math.toRadians(1))
			angle = Math.toRadians(1);

		double y1 = W / 2.0 * Math.tan(angle);
		double y2 = W / 2.0 * Math.tan(Math.PI / 2 - angle);

		path.reset();
		path.moveTo(W / 2f, 0);
		path.lineTo(0, 0);
		path.lineTo(0, (float) y1);
		canvas2.drawPath(path, sectorPaint);
		
		path.reset();
		path.moveTo(W / 2f, 0);
		path.lineTo(W / 2f, H);
		path.lineTo(0, H);
		path.lineTo(0, (float) y2);
		canvas2.drawPath(path, sectorPaint);
		
		path.reset();
		path.moveTo(W / 2f, 0);
		path.lineTo(W, 0);
		path.lineTo(W, (float) y1);
		canvas2.drawPath(path, sectorPaint);
		
		path.reset();
		path.moveTo(W / 2f, 0);
		path.lineTo(W / 2f, H);
		path.lineTo(W, H);
		path.lineTo(W, (float) y2);
		canvas2.drawPath(path, sectorPaint);
		
		if(y1 > y2) {
			path.reset();
			path.moveTo(W / 2f, 0);
			path.lineTo(0, (float) y2);
			path.lineTo(0, (float) y1);
			canvas2.drawPath(path, sectorPaint1);

			path.reset();
			path.moveTo(W / 2f, 0);
			path.lineTo(W, (float) y2);
			path.lineTo(W, (float) y1);
			canvas2.drawPath(path, sectorPaint1);
		}

		srcRect.set(0, 0, W, H);
		dstRect.set(edge, edge, getWidth() - edge, getHeight() - edge);
		canvas.drawBitmap(bmBg, srcRect, dstRect, null);

		dstRect.set(bgEdge, bgEdge, getWidth() - bgEdge, getHeight() - bgEdge);
		canvas.drawBitmap(bm, srcRect, dstRect, null);
	}

	public void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
		int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		setMeasuredDimension(size, size * 4 / 3);
	}
}
