package kvv.kvvplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class MyProgressBar extends View {

	private int max;
	private int current;

	public MyProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// if (max == 0)
		// return;
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);

		int w = getWidth();
		int h = getHeight();

		for (int i = 0; i < max; i++) {
			if (i == current)
				paint.setColor(Color.YELLOW);
			else
				paint.setColor(Color.GRAY);

			canvas.drawRect((float) w * i / max + 2, 2, (float) w * (i + 1)
					/ max - 2, h - 2, paint);
		}
	}

	public void setMax(int max) {
		this.max = max;
		invalidate();
	}

	public void setCurrent(int current) {
		this.current = current;
		invalidate();
	}
}
