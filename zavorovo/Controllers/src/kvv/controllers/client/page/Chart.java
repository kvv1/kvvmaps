package kvv.controllers.client.page;

import java.util.ArrayList;

import kvv.controllers.client.page.ScheduleCanvas.Range;
import kvv.controllers.history.shared.HistoryItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Composite;

public class Chart extends Composite {
	int width = 500;
	int height = 500;
	int chartWidth = width - 40;
	int chartHeight = height - 40;

	public Chart(Range sel, ArrayList<HistoryItem> logItems) {

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		
		for(HistoryItem item : logItems) {
			if(item.seconds >= sel.from && item.seconds < sel.to && item.value != null) {
				min = Math.min(min, item.value);
				max = Math.max(max, item.value);
			}
		}

		if(min >= max)
			return;
		
		
		
		
		
		Canvas canvas = Canvas.createIfSupported();
		Context2d context = canvas.getContext2d();

		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		context.save();
		context.setFillStyle("#808080");
		context.fillRect(0, 0, width, height);
		context.stroke();

		context.beginPath();
		context.setStrokeStyle("black");
		context.setLineWidth(1);

		for (int sec = sel.from / 600 * 600; sec < sel.to; sec += 600) {
			context.moveTo(sec2x(sec, sel), 0);
			context.lineTo(sec2x(sec, sel), chartHeight);
		}

		
		
		context.stroke();

		context.beginPath();
		context.setLineWidth(2);
		
		
		context.stroke();
		
		context.restore();

		initWidget(canvas);
	}

	int sec2x(int sec, Range sel) {
		return (sec - sel.from) * chartWidth / (sel.to - sel.from);
	}
}
