package kvv.sonar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SonarView extends View {

	// private int[] data;

	private float ax;
	private float ay;

	class Ball {
		public Ball(int x, int y, int z) {
			super();
			this.x = x;
			this.y = y;
			this.z = z;
		}

		int x;
		int y;
		int z;
	}

	private List<Ball> balls = new ArrayList<Ball>();

	public SonarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		for (int i = 0; i < 10; i++) {
			int x = (int) (Math.random() * 200 - 100);
			int y = (int) (Math.random() * 200 - 100);
			int z = (int) (Math.random() * 20);

			balls.add(new Ball(x, y, z));
		}

		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();

		canvas.drawColor(Color.GRAY);

		Paint paint = new Paint();

		paint.setColor(Color.WHITE);

		canvas.drawLine(0, 0, w, h, paint);
		canvas.drawLine(0, h, w, 0, paint);

		for (Ball ball : balls) {
			canvas.drawCircle(ball.x + ball.z * ax + w / 2, ball.y - ball.z
					* ay + h / 2, 20, paint);
		}

		super.onDraw(canvas);
	}

	public void set(float ax, float ay) {
		this.ax = ax;
		this.ay = ay;
		postInvalidate();
	}

}
