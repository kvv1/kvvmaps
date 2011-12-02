package kvv.sonar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

public class SonarView extends View {

	private int[] data;


	public SonarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();
		
		canvas.drawColor(Color.GRAY);
		
		Paint paint = new Paint();
		
		paint.setColor(Color.WHITE);
		
		canvas.translate(0, h - 10);
		canvas.drawLine(0, 0, w, 0, paint);
		
		if(data != null) {
			int max = 0;
			for(int i = 0; i < data.length; i++)
				max = Math.max(max, data[i]);
			
			for(int i = 0; i < data.length; i++) {
				int x = i * w / data.length;
				int y = - Math.abs(data[i]) * (h - 20) / max;
				canvas.drawLine(x, 0, x, y, paint);
			}
			
				
			
		}
		
//		canvas.drawLine(0, 0, w, h, paint);
//		canvas.drawLine(0, h, w, 0, paint);
		
		super.onDraw(canvas);
	}


	public void setData(int[] data) {
		this.data = data;
		invalidate();
	}
	
}
