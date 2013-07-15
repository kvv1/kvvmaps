package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.shared.ScheduleItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public class ScheduleCanvas extends Composite {
	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();
	private static final int tenMinutesWidth = 6;
	private static final int registerHeight = 20;
	private static final int topMargin = 4;
	private static final int bottomMargin = 16;

	private static final int width = tenMinutesWidth * 6 * 24;
	private static final int height = registerHeight + bottomMargin + topMargin;

	static Image bgImage;

	public ScheduleCanvas(final MouseMoveHandler mouseMoveHandler) {
		initWidget(canvas);

		canvas.setWidth(width + "px");
		canvas.setHeight(height + "px");
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		fillBackground();

		if (mouseMoveHandler != null) {
			canvas.addMouseMoveHandler(mouseMoveHandler);
			canvas.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					mouseMoveHandler.onMouseMove(null);
				}
			});
		}

	}

	private static int getRegY() {
		return topMargin;
	}

	private static int getY(int val, int minVal, int maxVal, int range) {
		return registerHeight - range * (val - minVal) / (maxVal - minVal);
	}

	private void fillBackground() {
		ImageElement imageElement = ImageElement.as(bgImage.getElement());
		context.drawImage(imageElement, 0, 0);
	}

	public void drawMarker(int x) {
		refresh();

		context.save();
		context.beginPath();
		context.setStrokeStyle("blue");
		context.setLineWidth(2);

		context.lineTo(x, 0);
		context.lineTo(x, height);

		context.stroke();
		context.closePath();
		context.restore();
	}

	private ArrayList<ScheduleItem> items;
	private Date date;

	public void refresh(ArrayList<ScheduleItem> items, Date date) {
		this.items = items;
		this.date = date;
		refresh();
	}

	private boolean refresh() {
		fillBackground();

		if (items == null)
			return false;

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
		context.closePath();
		context.beginPath();
		context.setStrokeStyle("yellow");

		@SuppressWarnings("deprecation")
		int minutes = date.getHours() * 60 + date.getMinutes();

		context.lineTo(minutes * tenMinutesWidth / 10, 0);
		context.lineTo(minutes * tenMinutesWidth / 10, registerHeight);

		context.stroke();
		context.closePath();
		context.restore();

		return true;
	}

	static {
		Canvas bgCanvas = Canvas.createIfSupported();
		Context2d context = bgCanvas.getContext2d();

		bgCanvas.setWidth(width + "px");
		bgCanvas.setHeight(height + "px");
		bgCanvas.setCoordinateSpaceWidth(width);
		bgCanvas.setCoordinateSpaceHeight(height);

		context.save();
		context.setFillStyle("#808080");
		context.fillRect(0, 0, width, height);
		context.stroke();
		context.restore();

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

		bgImage = new Image(bgCanvas.toDataUrl());
	}

}
