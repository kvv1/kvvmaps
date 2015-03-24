package kvv.aplayer.chart;

import java.util.Iterator;

import kvv.aplayer.chart.ChartData.Item;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ChartView extends View{

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private ChartData data;
	private final Paint paint = new Paint();
	
	public void setData(ChartData data) {
		this.data = data;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(data == null || data.data.isEmpty())
			return;

		paint.setColor(Color.RED);
		
		int n = data.data.size() / 2;
		
		long max = Long.MIN_VALUE; 
		long min = Long.MAX_VALUE; 
		
		long start = data.data.get(0).x;
		long stop = start;
		
		Iterator<Item> it = data.data.iterator();
		while(it.hasNext()) {
			Item i = it.next();
			max = Math.max(max, i.y);
			min = Math.min(min, i.y);
			stop = i.x;
		}

		if(max == min)
			max++;

		if(stop == start)
			stop++;

		it = data.data.iterator();
		
		Item prev = it.next();
		
		int w = getWidth();
		int h = getHeight();
		
		while(it.hasNext()) {
			Item i = it.next();
			
			float startX = (float) ((double)(prev.x - start) * w / (stop - start));
			float stopX = (float) ((double)(i.x - start) * w / (stop - start));

			float startY = (float) (h - (double)(prev.y - min) * h / (max - min));
			float stopY = (float) (h - (double)(i.y - min) * h / (max - min));

			canvas.drawLine(startX, startY, stopX, stopY, paint);
			
			prev = i;
		}
	}
}
