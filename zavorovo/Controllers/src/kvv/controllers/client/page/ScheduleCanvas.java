package kvv.controllers.client.page;

import java.util.ArrayList;

import kvv.controllers.history.shared.HistoryItem;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
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
		int from;
		int to;
	};

	//
	// private Range sel;

	private final int minVal;
	private final int maxVal;
	private final int[] scaleLevels;

	private final int registerHeight;
	private final int height;

	protected abstract void save(String regName,
			RegisterSchedule registerSchedule);

	public ScheduleCanvas(final Register reg,
			final MouseMoveHandler mouseMoveHandler) {
		scaleLevels = reg.scaleLevels;

		minVal = scaleLevels[0];
		maxVal = scaleLevels[scaleLevels.length - 1];

		if (reg.height == null)
			registerHeight = 18;
		else
			registerHeight = reg.height;

		height = registerHeight + bottomMargin + topMargin;

		createBG();

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

		canvas.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				int m = x2sec(event.getX()) / 60;

				if ((m < 0 || m >= 24 * 60))
					return;

				int minute = (m + 5) / 10 * 10;
				double y0 = getY(minVal) + getRegY();
				double y1 = getY(maxVal) + getRegY();

				if (Math.abs(event.getY() - y0) < 3) {
					if (!ModePage.check())
						return;

					if (registerSchedule == null)
						registerSchedule = new RegisterSchedule();
					registerSchedule.add(minute, minVal);
					if (registerSchedule.items.size() == 0)
						registerSchedule = null;
					save(reg.name, registerSchedule);
					// setDirty(true);
					refresh();
				}
				if (Math.abs(event.getY() - y1) < 3) {
					if (!ModePage.check())
						return;

					if (registerSchedule == null)
						registerSchedule = new RegisterSchedule();
					registerSchedule.add(minute, maxVal);
					if (registerSchedule.items.size() == 0)
						registerSchedule = null;
					save(reg.name, registerSchedule);
					// setDirty(true);
					refresh();
				}

				// sel = new Range();
				// sel.from = sel.to = x2sec(event.getX());
			}
		});

		canvas.addMouseUpHandler(new MouseUpHandler() {

			@Override
			public void onMouseUp(MouseUpEvent event) {
				// final DialogBox dialog = new DialogBox();
				// final Button ok = new Button("OK");
				// ok.addClickHandler(new ClickHandler() {
				// public void onClick(ClickEvent event) {
				// dialog.hide();
				// }
				// });
				//
				// final VerticalPanel panel = new VerticalPanel();
				// panel.setSpacing(10);
				// panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				//
				// final Range sel1 = sel;
				//
				// panel.add(new Chart(sel1, logItems));
				// panel.add(ok);
				//
				// dialog.setWidget(panel);
				//
				// dialog.show();
				//
				// sel = null;
				// refresh();
			}
		});

		canvas.addMouseMoveHandler(new MouseMoveHandler() {

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				// if (sel != null) {
				// sel.to = Math.max(sel.from, x2sec(event.getX()));
				// refresh();
				// }
			}
		});
	}

	private int x2sec(int x) {
		return x * 10 * 60 / tenMinutesWidth;
	}

	private int sec2x(int sec) {
		return sec * tenMinutesWidth / 10 / 60;
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
	private int historyEndSeconds;
	private ArrayList<HistoryItem> logItems;

	public void refresh(RegisterSchedule registerSchedule,
			ArrayList<HistoryItem> logItems, int curSeconds,
			int historyEndSeconds) {
		this.registerSchedule = registerSchedule;
		this.logItems = logItems;
		this.curSeconds = curSeconds;
		this.historyEndSeconds = historyEndSeconds;
		refresh();
	}

	private boolean refresh() {
		fillBackground();

		context.save();
		context.translate(0, getRegY());

		if (registerSchedule != null) {
			int val = registerSchedule.getValue(0);
			context.beginPath();
			context.setStrokeStyle("#00FF00");
			context.setFillStyle("#00FF00");
			context.setLineWidth(2);

			context.moveTo(0, getY(val));
			for (ScheduleItem item : registerSchedule.items) {
				int x = sec2x(item.minutes * 60);
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

			HistoryItem lastValue = null;

			for (HistoryItem logItem : logItems) {
				drawLogItem(logItem.seconds, logItem, lastValue);
				lastValue = logItem;
			}
			drawLogItem(historyEndSeconds, lastValue, lastValue);

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

	private void drawLogItem(int seconds, HistoryItem logItem,
			HistoryItem lastValue) {
		Integer last = lastValue == null ? null : lastValue.value;
		Integer current = logItem.value;

		int x = sec2x(seconds);

		if (last == null && current != null) {
			context.moveTo(x, getY(current));
		} else if (last != null && current != null) {
			context.lineTo(x, getY(last));
			context.lineTo(x, getY(current));
		} else if (last != null && current == null) {
			context.lineTo(x, getY(last));
		}
		/*
		 * if (last != null) context.lineTo(x, getY(last));
		 * 
		 * if (current != null) { if (last == null) context.moveTo(x,
		 * getY(current)); else context.lineTo(x, getY(current)); }
		 */
	}

	private void createBG() {
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

		for (int x = tenMinutesWidth; x <= tenMinutesWidth * 6 * 24; x += tenMinutesWidth) {
			context.moveTo(x, getRegY());
			context.lineTo(x, getRegY() + registerHeight);
		}

		if (scaleLevels.length != 2 || scaleLevels[0] != 0
				|| scaleLevels[1] != 1)
			for (int level : scaleLevels) {
				double y = getY(level) + getRegY();
				context.moveTo(0, y);
				context.lineTo(tenMinutesWidth * 6 * 24, y);
			}

		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setLineWidth(1);

		if (scaleLevels.length != 2 || scaleLevels[0] != 0
				|| scaleLevels[1] != 1)
			for (int level : scaleLevels) {
				double y = getY(level) + getRegY();
				context.strokeText("" + level, tenMinutesWidth * 6 * 24 + 4,
						y + 4);
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
