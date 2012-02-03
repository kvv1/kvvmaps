package kvv.kvvmap.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class Compass {

	private Path arrowPathN = new Path();
	private Path arrowPathS = new Path();
	private Path arrowPathT = new Path();

	private float[] compassValues;

	public Compass(int sz) {
		arrowPathN.moveTo(0, -sz);
		arrowPathN.lineTo(-sz / 5, 0);
		arrowPathN.lineTo(sz / 5, 0);
		arrowPathN.close();

		arrowPathS.moveTo(0, sz);
		arrowPathS.lineTo(-sz / 5, 0);
		arrowPathS.lineTo(sz / 5, 0);
		arrowPathS.close();

		arrowPathT.moveTo(0, sz / 2);
		arrowPathT.lineTo(-sz / 5, 0);
		arrowPathT.lineTo(sz / 5, 0);
		arrowPathT.close();
	}

	public void drawCompass(Canvas canvas, Paint paint, Point pt,
			Float targBearing) {
		if (compassValues != null) {
			paint.setStyle(Paint.Style.FILL);
			canvas.translate(pt.x, pt.y);

			canvas.rotate(-compassValues[0]);

			paint.setColor(Color.BLUE);
			canvas.drawPath(arrowPathN, paint);
			paint.setColor(Color.RED);
			canvas.drawPath(arrowPathS, paint);

			if (targBearing != null) {
				paint.setColor(Color.MAGENTA);
				paint.setStrokeWidth(2);
				canvas.rotate(targBearing + 180);
				canvas.drawPath(arrowPathT, paint);
				canvas.rotate(-(targBearing + 180));
			}

			canvas.rotate(compassValues[0]);
			canvas.translate(-pt.x, -pt.y);
		}
	}

	public void setValues(float[] values) {
		compassValues = values;
	}
}
