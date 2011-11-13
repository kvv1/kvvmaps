package kvv.kvvmap.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class Compass {

	private Path arrowPathN = new Path();
	private Path arrowPathS = new Path();

	private float[] compassValues;
	
	private final int sz;

	public Compass(int sz) {
		this.sz = sz;
		
		arrowPathN.moveTo(0, -sz);
		arrowPathN.lineTo(-sz / 5, 0);
		arrowPathN.lineTo(sz / 5, 0);
		arrowPathN.close();

		arrowPathS.moveTo(0, sz);
		arrowPathS.lineTo(-sz / 5, 0);
		arrowPathS.lineTo(sz / 5, 0);
		arrowPathS.close();
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

			int sz1 = sz * 80 / 100;
			int sz2 = sz * 25 / 100;
			int sz3 = sz * 15 / 100;
			
			if (targBearing != null) {
				paint.setColor(Color.MAGENTA);
				paint.setStrokeWidth(2);
				canvas.rotate(targBearing);
				canvas.drawLine(0, 0, 0, -sz1, paint);
				canvas.drawLine(0, -sz1, -sz3, -sz1 + sz2, paint);
				canvas.drawLine(0, -sz1, sz3, -sz1 + sz2, paint);
			}
			
			
		}
	}

	public void setValues(float[] values) {
		compassValues = values;
	}
}
