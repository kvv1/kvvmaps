package kvv.kvvmap.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Arrow {

	private Path arrowPath = new Path();

	public Arrow(int sz) {
		arrowPath.moveTo(0, -sz);
		arrowPath.lineTo(-sz / 2, sz);
		arrowPath.lineTo(sz / 2, sz);
		arrowPath.close();
	}

	public void draw(Canvas canvas, float x, float y, float rot, boolean dimmed) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		System.out.println("" + rot);
		
		paint.setStyle(Paint.Style.FILL);

		canvas.translate(x, y);
		canvas.rotate(rot);

		if(dimmed)
			paint.setColor(Color.RED / 2);
		else
			paint.setColor(Color.RED);
		
		paint.setAlpha(150);
		
		canvas.drawPath(arrowPath, paint);

		canvas.rotate(-rot);
		canvas.translate(-x, -y);
	}

}
