package kvv.controllers.client.control.simple;

import java.util.ArrayList;

import kvv.controllers.shared.HistoryItem;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.State;
import kvv.controllers.shared.ScheduleItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;

public abstract class ScheduleCanvas extends Composite {
	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();
	private final int minFrom;
	private final int minTo;
	private final int scaleStepMinutes;
	private final boolean lens;

	private int[] sel;

	private static final int topMargin = 4;
	private static final int bottomMargin = 14;

	private final int width1;
	private final int width;

	// private Image bgImage;

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

	protected abstract void save(RegisterSchedule registerSchedule,
			String comment);

	public ScheduleCanvas(final RegisterPresentation _presentation,
			final MouseMoveHandler mouseMoveHandler, int width, int minFrom,
			int minTo, int scaleStepMinutes, boolean lens) {

		this.regName = _presentation.name;

		this.minFrom = minFrom;
		this.minTo = minTo;
		this.scaleStepMinutes = scaleStepMinutes;
		this.lens = lens;

		this.width1 = width;
		this.width = width + (lens ? 0 : 30);

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
			registerHeight = 18 * (lens ? 2 : 1);
		else
			registerHeight = _presentation.height * (lens ? 2 : 1);

		height = registerHeight + bottomMargin + topMargin;

		initWidget(canvas);

		canvas.setPixelSize(this.width, this.height);
		canvas.setCoordinateSpaceWidth(this.width);
		canvas.setCoordinateSpaceHeight(this.height);

		createBG(canvas, context);
		// fillBackground();

		if (mouseMoveHandler != null) {
			canvas.addMouseMoveHandler(mouseMoveHandler);
			canvas.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					mouseMoveHandler.onMouseMove(null);
				}
			});
		}

		if (!lens) {
			canvas.addDomHandler(new DoubleClickHandler() {
				@Override
				public void onDoubleClick(final DoubleClickEvent event) {
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
					refresh();
				}
			}, DoubleClickEvent.getType());

			canvas.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
						int x = event.getX();
						int sec = x2sec(x);

						int w = 400;

						sel = new int[] { sec / 60 - 40, sec / 60 + 40 };
						refresh();

						final ScheduleCanvas scheduleCanvas1 = new ScheduleCanvas(
								_presentation, null, w, sel[0], sel[1], 1, true) {
							@Override
							protected void save(
									RegisterSchedule registerSchedule,
									String comment) {
							}

						};

						scheduleCanvas1.refresh(registerSchedule, logItems,
								curSeconds);

						new PopupPanel(true) {
							{
								setWidget(scheduleCanvas1);
							}

							protected void onDetach() {
								sel = null;
								refresh();
								super.onDetach();
							}
						}.showRelativeTo(canvas);
					}
				}
			});
		}
	}

	private int x2sec(int x) {
		return minFrom * 60 + (minTo - minFrom) * 60 * x / width1;
	}

	private double sec2x(int sec) {
		return (sec / 60.0 - minFrom) * width1 / (minTo - minFrom);
	}

	private static int getRegY() {
		return topMargin;
	}

	private double getY(int val) {
		return registerHeight - (double) registerHeight * (val - minVal)
				/ (maxVal - minVal);
	}

	// private void fillBackground() {
	// ImageElement imageElement = ImageElement.as(bgImage.getElement());
	// context.drawImage(imageElement, 0, 0);
	// }

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

//	private/* final static */String schedColor = "#00FF00";
//	private/* final static */String historyColor = "#00FFFF";

	private/* final static */String schedColor = "lime";
	private/* final static */String historyColor = "cyan";
	
	private/* final static */String bgColor = "#808080";
	private/* final static */String bgSelColor = "#A0A0A0";

	private/* final static */String timeColor = "yellow";

	private boolean refresh() {
		createBG(canvas, context);
		// fillBackground();

		context.save();
		context.translate(0, getRegY());

		if (registerSchedule != null && registerSchedule.items != null
				&& registerSchedule.items.size() != 0) {
			int val = registerSchedule.getValue(0);
			context.beginPath();
			context.setStrokeStyle(schedColor);
			context.setFillStyle(schedColor);
			context.setLineWidth(2);

			context.moveTo(sec2x(0), getY(val));
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

		if (logItems != null) {
			context.beginPath();
			context.setStrokeStyle(historyColor);
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
		context.setStrokeStyle(timeColor);
		context.setLineWidth(2);

		context.lineTo(sec2x(curSeconds), 0);
		context.lineTo(sec2x(curSeconds), registerHeight);

		context.stroke();
		context.closePath();

		context.restore();

		return true;
	}

	private void createBG(Canvas bgCanvas, Context2d context) {
		// Canvas bgCanvas = Canvas.createIfSupported();
		// Context2d context = bgCanvas.getContext2d();

		bgCanvas.setPixelSize(width, height);
		bgCanvas.setCoordinateSpaceWidth(width);
		bgCanvas.setCoordinateSpaceHeight(height);

		context.save();
		context.setFillStyle(bgColor);
		context.fillRect(0, 0, width, height);

		if (sel != null) {
			context.setFillStyle(bgSelColor);
			context.fillRect(sec2x(sel[0] * 60), 0,
					sec2x((sel[1] - sel[0]) * 60), height);
		}

		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setStrokeStyle("black");
		context.setLineWidth(1);

		for (int min = 0; min <= 60 * 24; min += scaleStepMinutes) {
			double x = sec2x(min * 60);
			if (min < minFrom || min > minTo)
				continue;
			context.moveTo(x, getRegY());
			context.lineTo(x, getRegY() + registerHeight);
		}

		if (!isBool && maxVal > minVal && step > 0) {
			for (int level = minVal; level <= maxVal; level += step) {
				double y = getY(level) + getRegY();
				context.moveTo(sec2x(0), y);
				context.lineTo(sec2x(60 * 60 * 24), y);
			}
		}

		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setLineWidth(1);

		if (!lens && !isBool && maxVal > minVal && step > 0) {
			for (int level = minVal; level <= maxVal; level += step) {
				double y = getY(level) + getRegY();
				context.strokeText("" + level, sec2x(minTo * 60) + 4, y + 4);
			}
		}

		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		context.setLineWidth(2);
		for (int hour = 1; hour <= 24; hour++) {
			double x = sec2x(hour * 60 * 60);
			context.moveTo(x, getRegY());
			context.lineTo(x, height - bottomMargin);
		}
		context.stroke();
		context.restore();

		context.save();
		context.beginPath();
		// context.setLineWidth(1);
		for (int hour = 1; hour < 24; hour++) {
			double x = sec2x(hour * 60 * 60);
			context.strokeText("" + hour, x - 5, height - 3);
		}
		context.restore();

		// bgImage = new Image(bgCanvas.toDataUrl());
	}

}
