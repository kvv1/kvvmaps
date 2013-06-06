package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.shared.ScheduleItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.Composite;

public class ScheduleCanvas extends Composite {
	private Canvas canvas = Canvas.createIfSupported();
	private Context2d context = canvas.getContext2d();
	private int tenMinutesWidth = 6;
	private int registerHeight = 20;
	private int topMargin = 4;
	private int bottomMargin = 16;

	int width;
	int height;

	public ScheduleCanvas() {
		initWidget(canvas);

		width = getW();
		height = getH();

		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		fillBackground();
	}

	int getW() {
		return tenMinutesWidth * 6 * 24;
	}

	int getH() {
		return registerHeight + bottomMargin + topMargin;
	}

	int getRegY() {
		return topMargin;
	}

	int getY(int val, int minVal, int maxVal, int range) {
		return registerHeight - range * (val - minVal) / (maxVal - minVal);
	}

	private void fillBackground() {
		context.save();
		context.setFillStyle("#808080");
		context.fillRect(0, 0, width, height);
		context.stroke();
		context.restore();
	}

	public void refresh(ArrayList<ScheduleItem> items) {
		fillBackground();

		context.save();
		context.beginPath();
		context.setStrokeStyle("black");
		context.setLineWidth(1);

		for (int x = tenMinutesWidth; x < tenMinutesWidth * 6 * 24; x += tenMinutesWidth) {
			context.moveTo(x, getRegY());
			context.lineTo(x, getRegY() + registerHeight);
		}
		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setLineWidth(2);
		for (int h = 1; h < 24; h++) {
			int x = h * tenMinutesWidth * 6;
			context.moveTo(x, getRegY());
			context.lineTo(x, height - bottomMargin);
		}
		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		// context.setLineWidth(1);
		for (int h = 1; h < 24; h++) {
			int x = h * tenMinutesWidth * 6;
			context.strokeText("" + h, x - 5, height - 3);
		}
		context.restore();

		int val = 0;
		int minVal = Integer.MAX_VALUE;
		int maxVal = Integer.MIN_VALUE;
		for (ScheduleItem item : items) {
			val = item.value;
			minVal = Math.min(minVal, val);
			maxVal = Math.max(maxVal, val);
		}

		if (minVal > maxVal) {
			minVal = 0;
			maxVal = 1;
			val = 0;
		}

		context.save();
		context.beginPath();
		context.setStrokeStyle("#00FF00");
		context.setLineWidth(2);
		context.translate(0, getRegY());

		context.moveTo(0, getY(val, minVal, maxVal, registerHeight));
		for (ScheduleItem item : items) {
			context.lineTo(item.minutes * tenMinutesWidth / 10,
					getY(val, minVal, maxVal, registerHeight));
			context.lineTo(item.minutes * tenMinutesWidth / 10,
					getY(item.value, minVal, maxVal, registerHeight));
			val = item.value;
		}
		context.lineTo(60 * 24 * tenMinutesWidth / 10,
				getY(val, minVal, maxVal, registerHeight));

		context.stroke();
		context.beginPath();
		context.setStrokeStyle("yellow");

		Date date = new Date();
		@SuppressWarnings("deprecation")
		int minutes = date.getHours() * 60 + date.getMinutes();

		context.lineTo(minutes * tenMinutesWidth / 10, 0);
		context.lineTo(minutes * tenMinutesWidth / 10, registerHeight);

		context.stroke();
		context.restore();

	}
}
