package kvv.aplayer.files.tape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class BobbinBmp {
	static public final int bmSize = 256;

	static final int color = 0xFFE0F0E0;
	static final int color1 = 0xFFB0C0B0;

	static public final int tapeMinR = 30;
	static public final int tapeMaxR = 115;

	static final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
	static final Bitmap bobbinBmp = Bitmap.createBitmap(bmSize, bmSize, conf);

	static {
		Bitmap bmp = Bitmap.createBitmap(bmSize, bmSize, conf);
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
		canvas.drawCircle(0, 0, tapeMinR, paint);

		paint.setColor(0xFF404040);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2);
		canvas.drawArc(new RectF(-5, -35, 5, -25), 0, 180, false, paint);

		Canvas canvas2 = new Canvas(bobbinBmp);
		Paint bobbinPaint1 = new Paint(Paint.FILTER_BITMAP_FLAG);
		bobbinPaint1.setAlpha(200);
		canvas2.drawBitmap(bmp, 0, 0, bobbinPaint1);
		canvas2.translate(cx, cy);

		paint.setColor(0xFF000000);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		canvas2.drawCircle(0, 0, 4, paint);
		paint.setStrokeWidth(3);
		canvas2.drawLine(0, 0, 0, 9, paint);
		canvas2.rotate(120);
		canvas2.drawLine(0, 0, 0, 9, paint);
		canvas2.rotate(120);
		canvas2.drawLine(0, 0, 0, 9, paint);

	}
	
	public static Bitmap getInstance() {
		return bobbinBmp;
	}

}
