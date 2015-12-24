package kvv.controllers.client.control.simple;

import java.util.ArrayList;

import kvv.controllers.shared.HistoryItem;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.State;
import kvv.controllers.shared.ScheduleItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;

public abstract class ScheduleCanvas extends Composite {
	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();
	private static final int tenMinutesWidth = 6;
	private static final int topMargin = 4;
	private static final int bottomMargin = 14;

	private static final int width = tenMinutesWidth * 6 * 24 + 30;

	private Image bgImage;

	public static class Range {
		public int from;
		public int to;
	};

	private final int minVal;
	private final int maxVal;
	private final int step;

	private final int registerHeight;
	private final int height;

	private boolean isBool;

	private String regName;

	protected abstract void save(RegisterSchedule registerSchedule, String comment);

	public ScheduleCanvas(final RegisterPresentation _presentation,
			final MouseMoveHandler mouseMoveHandler) {

		this.regName = _presentation.name;

		isBool = _presentation.isBool();
		if (isBool) {
			minVal = 0;
			maxVal = 1;
			step = 1;
		} else {
			minVal = _presentation.min;
			maxVal = _presentation.max;
			step = _presentation.step;
		}

		if (_presentation.height == null)
			registerHeight = 18;
		else
			registerHeight = _presentation.height;

		height = registerHeight + bottomMargin + topMargin;

		createBG();

		initWidget(canvas);

		canvas.setPixelSize(width, height);
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

		canvas.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (registerSchedule == null)
					return;

				int m = x2sec(event.getX()) / 60;

				if ((m < 0 || m >= 24 * 60))
					return;

				int minute = (m + 5) / 10 * 10;
				double y0 = getY(minVal) + getRegY();
				double y1 = getY(maxVal) + getRegY();

				double y = event.getY();

				int val = (int) (minVal + (y - y0) * (maxVal - minVal)
						/ (y1 - y0) + 0.5);

				String comment = minute / 60 + ":" + minute % 60;
				
				registerSchedule.add(minute, val);
				if (registerSchedule.items.size() == 0
						&& registerSchedule.state == State.SCHEDULE) {
					registerSchedule.state = State.MANUAL;
					comment += ", state := State.MANUAL";
				}
				
				save(registerSchedule, comment);
				// setDirty(true);
				refresh();

			}
		});

	}

	private int x2sec(int x) {
		return x * 10 * 60 / tenMinutesWidth;
	}

	private double sec2x(int sec) {
		return sec * tenMinutesWidth / 10f / 60;
	}

	private static int getRegY() {
		return topMargin;
	}

	private double getY(int val) {
		return registerHeight - (double) registerHeight * (val - minVal)
				/ (maxVal - minVal);
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
		context.setLineWidth(1);

		context.lineTo(x, 0);
		context.lineTo(x, height);

		context.stroke();
		context.closePath();
		context.restore();
	}

	private RegisterSchedule registerSchedule;
	private int curSeconds;
	private ArrayList<HistoryItem> logItems;

	public void refresh(RegisterSchedule registerSchedule,
			ArrayList<HistoryItem> logItems, int curSeconds) {
		this.registerSchedule = new RegisterSchedule(registerSchedule);
		this.logItems = logItems;
		this.curSeconds = curSeconds;
		refresh();
	}

	public void refresh(RegisterSchedule registerSchedule) {
		this.registerSchedule = new RegisterSchedule(registerSchedule);
		refresh();
	}

	private boolean refresh() {
		fillBackground();

		context.save();
		context.translate(0, getRegY());

		if (registerSchedule != null && registerSchedule.items != null
				&& registerSchedule.items.size() != 0) {
			int val = registerSchedule.getValue(0);
			context.beginPath();
			context.setStrokeStyle("#00FF00");
			context.setFillStyle("#00FF00");
			context.setLineWidth(2);

			context.moveTo(0, getY(val));
			for (ScheduleItem item : registerSchedule.items) {
				double x = sec2x(item.minutes * 60);
				double y = getY(val);
				context.lineTo(x, y);
				y = getY(item.value);
				context.lineTo(x, y);
				context.fillRect(x - 3, y - 3, 6, 6);
				val = item.value;
			}
			context.lineTo(sec2x(60 * 24 * 60), getY(val));

			context.stroke();
			context.closePath();
		}

		// if (sel != null) {
		// context.beginPath();
		// context.setStrokeStyle("#FFFF00");
		// context.setLineWidth(2);
		// context.moveTo(sec2x(sel.from), -2);
		// context.lineTo(sec2x(sel.to), -2);
		// context.stroke();
		// context.closePath();
		// }

		if (logItems != null) {
			context.beginPath();
			context.setStrokeStyle("#00FFFF");
			context.setLineWidth(2);

			Integer last = null;

			for (HistoryItem logItem : logItems) {
				double x = sec2x(logItem.seconds);
				Integer current = logItem.value;

				if (logItem.name == null) {
					last = null;
				} else {
					if (regName.equals(logItem.name)) {
						if (current != null) {
							if (last != null) {
								context.lineTo(x, getY(last));
								context.lineTo(x, getY(current));
							} else {
								context.moveTo(x, getY(current));
							}
						}
						last = current;
					} else {
						if (last != null)
							context.lineTo(x, getY(last));
					}
				}
			}

			context.stroke();
			context.closePath();
		}

		context.beginPath();
		context.setStrokeStyle("yellow");
		context.setLineWidth(2);

		context.lineTo(sec2x(curSeconds), 0);
		context.lineTo(sec2x(curSeconds), registerHeight);

		context.stroke();
		context.closePath();

		context.restore();

		return true;
	}

	private void createBG() {
		Canvas bgCanvas = Canvas.createIfSupported();
		Context2d context = bgCanvas.getContext2d();

		bgCanvas.setPixelSize(width, height);
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

		for (int x = tenMinutesWidth; x <= tenMinutesWidth * 6 * 24; x += tenMinutesWidth) {
			context.moveTo(x, getRegY());
			context.lineTo(x, getRegY() + registerHeight);
		}

		if (!isBool && maxVal > minVal && step > 0) {
			for (int level = minVal; level <= maxVal; level += step) {
				double y = getY(level) + getRegY();
				context.moveTo(0, y);
				context.lineTo(tenMinutesWidth * 6 * 24, y);
			}
		}

		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setLineWidth(1);

		if (!isBool && maxVal > minVal && step > 0) {
			for (int level = minVal; level <= maxVal; level += step) {
				double y = getY(level) + getRegY();
				context.strokeText("" + level, tenMinutesWidth * 6 * 24 + 4,
						y + 4);
			}
		}

		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setLineWidth(2);
		for (int hour = 1; hour <= 24; hour++) {
			int x = hour * tenMinutesWidth * 6;
			context.moveTo(x, getRegY());
			context.lineTo(x, height - bottomMargin);
		}
		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		// context.setLineWidth(1);
		for (int hour = 1; hour < 24; hour++) {
			int x = hour * tenMinutesWidth * 6;
			context.strokeText("" + hour, x - 5, height - 3);
		}
		context.restore();

		bgImage = new Image(bgCanvas.toDataUrl());
	}

}
