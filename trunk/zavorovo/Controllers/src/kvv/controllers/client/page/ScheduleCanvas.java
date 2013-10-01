package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.shared.HistoryItem;
import kvv.controllers.shared.ScheduleItem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ScheduleCanvas extends Composite {
	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();
	private static final int tenMinutesWidth = 6;
	private static final int registerHeight = 20;
	private static final int topMargin = 4;
	private static final int bottomMargin = 16;

	private static final int width = tenMinutesWidth * 6 * 24;
	private static final int height = registerHeight + bottomMargin + topMargin;

	private static Image bgImage;

	public static class Range {
		int from;
		int to;
	};

	private Range sel;

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

		canvas.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				sel = new Range();
				sel.from = sel.to = x2sec(event.getX());
			}
		});

		canvas.addMouseUpHandler(new MouseUpHandler() {

			@Override
			public void onMouseUp(MouseUpEvent event) {
				final DialogBox dialog = new DialogBox();
				final Button ok = new Button("OK");
				ok.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						dialog.hide();
					}
				});

				final VerticalPanel panel = new VerticalPanel();
				panel.setSpacing(10);
				panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

				final Range sel1 = sel;

				panel.add(new Chart(sel1, logItems));
				panel.add(ok);

				// scheduleService.getLog(new CallbackAdapter<Log>() {
				// @Override
				// public void onSuccess(Log log) {
				// panel.add(new Chart(sel1, log.items.get(reg)));
				// panel.add(ok);
				// }
				// });

				dialog.setWidget(panel);

				dialog.show();

				sel = null;
				refresh();
			}
		});

		canvas.addMouseMoveHandler(new MouseMoveHandler() {

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				if (sel != null) {
					sel.to = Math.max(sel.from, x2sec(event.getX()));
					refresh();
				}
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
	private ArrayList<HistoryItem> logItems;

	public void refresh(ArrayList<ScheduleItem> items,
			ArrayList<HistoryItem> logItems, Date date) {
		this.items = items;
		this.logItems = logItems;
		this.date = date;
		refresh();
	}

	private boolean refresh() {
		fillBackground();

		int val = 0;
		int minVal = Integer.MAX_VALUE;
		int maxVal = Integer.MIN_VALUE;

		if (items != null)
			for (ScheduleItem item : items) {
				val = item.value;
				minVal = Math.min(minVal, val);
				maxVal = Math.max(maxVal, val);
			}

		if (logItems != null)
			for (HistoryItem logItem : logItems) {
				int logVal = logItem.value;
				minVal = Math.min(minVal, logVal);
				maxVal = Math.max(maxVal, logVal);
			}

		if (minVal > maxVal) {
			minVal = 0;
			maxVal = 1;
			val = 0;
		}

		if (minVal == maxVal)
			maxVal = minVal + 1;

		context.save();
		context.translate(0, getRegY());

		if (items != null) {
			context.beginPath();
			context.setStrokeStyle("#00FF00");
			context.setLineWidth(2);

			context.moveTo(0, getY(val, minVal, maxVal, registerHeight));
			for (ScheduleItem item : items) {
				context.lineTo(sec2x(item.minutes * 60),
						getY(val, minVal, maxVal, registerHeight));
				context.lineTo(sec2x(item.minutes * 60),
						getY(item.value, minVal, maxVal, registerHeight));
				val = item.value;
			}
			context.lineTo(sec2x(60 * 24 * 60),
					getY(val, minVal, maxVal, registerHeight));

			context.stroke();
			context.closePath();
		}

		if (sel != null) {
			context.beginPath();
			context.setStrokeStyle("#FFFF00");
			context.setLineWidth(2);
			context.moveTo(sec2x(sel.from), -2);
			context.lineTo(sec2x(sel.to), -2);
			context.stroke();
			context.closePath();
		}

		if (logItems != null) {
			context.beginPath();
			context.setStrokeStyle("#00FFFF");
			context.setLineWidth(2);

			Integer lastValue = null;

			for (HistoryItem logItem : logItems) {
				int x = sec2x(logItem.seconds);

				if (lastValue != null)
					context.lineTo(x,
							getY(lastValue, minVal, maxVal, registerHeight));

				if (logItem.value != null) {
					if (lastValue == null)
						context.moveTo(
								x,
								getY(logItem.value, minVal, maxVal,
										registerHeight));
					else
						context.lineTo(
								x,
								getY(logItem.value, minVal, maxVal,
										registerHeight));
				}

				lastValue = logItem.value;
			}

			context.stroke();
			context.closePath();
		}

		if (date != null) {
			context.beginPath();
			context.setStrokeStyle("yellow");
			context.setLineWidth(2);

			@SuppressWarnings("deprecation")
			int minutes = date.getHours() * 60 + date.getMinutes();

			context.lineTo(sec2x(minutes * 60), 0);
			context.lineTo(sec2x(minutes * 60), registerHeight);

			context.stroke();
			context.closePath();
		}

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
