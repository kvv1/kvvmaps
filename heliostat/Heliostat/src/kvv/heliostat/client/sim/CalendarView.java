package kvv.heliostat.client.sim;

import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public abstract class CalendarView extends Composite implements View {

	protected final CalendarCanvas calendarCanvas = new CalendarCanvas();

	private final Canvas dcanvas = Canvas.createIfSupported();
	private final Context2d dcontext = dcanvas.getContext2d();

	private final Canvas tcanvas = Canvas.createIfSupported();
	private final Context2d tcontext = tcanvas.getContext2d();

	private final Canvas canvas1 = Canvas.createIfSupported();
	private final Context2d context1 = canvas1.getContext2d();

	private AbsolutePanel p = new AbsolutePanel();

	private final static int leftMargin = 30;
	private final static int bottomMargin = 12;

	protected abstract void drawContent();

	protected abstract int getFirstDay();

	protected void onMouseDown(int x, int y, int dayOffset, double time) {
	}

	protected void onMouseUp(int x, int y) {
	}

	protected void onMouseMove(int x, int y) {
	}

	protected void onDayClicked(int day) {
	}

	protected void onTimeClicked(double time) {
	}

	public CalendarView(Model model) {
		model.add(this);

		canvas1.setPixelSize(2, CalendarCanvas.dayHeight);
		canvas1.setCoordinateSpaceWidth(2);
		canvas1.setCoordinateSpaceHeight(CalendarCanvas.dayHeight);

		context1.beginPath();
		context1.setFillStyle("black");
		context1.fillRect(0, 0, 2, CalendarCanvas.dayHeight);
		context1.closePath();

		tcanvas.setPixelSize(calendarCanvas.width, bottomMargin);
		tcanvas.setCoordinateSpaceWidth(calendarCanvas.width);
		tcanvas.setCoordinateSpaceHeight(bottomMargin);

		dcanvas.setPixelSize(leftMargin, calendarCanvas.height + bottomMargin);
		dcanvas.setCoordinateSpaceWidth(leftMargin);
		dcanvas.setCoordinateSpaceHeight(calendarCanvas.height + bottomMargin);

		p.add(calendarCanvas.canvas);
		p.add(tcanvas);
		p.add(dcanvas);
		p.add(canvas1);

		p.setWidgetPosition(tcanvas, leftMargin, calendarCanvas.height);
		p.setWidgetPosition(dcanvas, 0, 0);
		p.setWidgetPosition(calendarCanvas.canvas, leftMargin, 0);

		p.setWidth(calendarCanvas.width + leftMargin + "px");
		p.setHeight(calendarCanvas.height + bottomMargin + "px");

		calendarCanvas.canvas.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				int d = calendarCanvas.y2d(event.getY());
				double t = calendarCanvas.x2t(event.getX());
				CalendarView.this.onMouseDown(event.getX(), event.getY(), d, t);
			}
		});

		calendarCanvas.canvas.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				CalendarView.this.onMouseUp(event.getX(), event.getY());
			}
		});

		calendarCanvas.canvas.addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				CalendarView.this.onMouseMove(event.getX(), event.getY());
			}
		});

		dcanvas.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				int d = calendarCanvas.y2d(event.getY());
				onDayClicked((d + getFirstDay()) % 365);
			}
		});

		tcanvas.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				onTimeClicked(calendarCanvas.x2t(event.getX()));
			}
		});

		initWidget(p);
	}

	public void draw() {
		calendarCanvas.clearGrid();
		drawContent();
		calendarCanvas.drawGrid();
		drawGrid();
	}

	protected void drawGrid() {

		tcontext.beginPath();
		tcontext.setFillStyle("gray");
		tcontext.fillRect(0, 0, calendarCanvas.width, bottomMargin);
		tcontext.setFillStyle("yellow");

		for (int t = calendarCanvas.sunrise + 1; t < calendarCanvas.sunset; t++) {
			double x = calendarCanvas.t2x(t);
			String text = "" + t;
			TextMetrics tm = tcontext.measureText(text);
			tcontext.fillText(text, x - tm.getWidth() / 2, bottomMargin - 2);
		}

		dcontext.stroke();
		dcontext.closePath();

		dcontext.beginPath();
		dcontext.setFillStyle("gray");
		dcontext.fillRect(0, 0, leftMargin, calendarCanvas.height
				+ bottomMargin);
		dcontext.setFillStyle("yellow");

		for (int d = 0; d < calendarCanvas.days; d++) {
			dcontext.fillText("" + (d + getFirstDay()) % 365, 4,
					CalendarCanvas.dayHeight * (d + 1));
		}

		dcontext.stroke();
		dcontext.closePath();
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;
		double x = calendarCanvas.t2x(state.time) + leftMargin;
		p.setWidgetPosition(canvas1, (int) (x + 0.5) - 1,
				calendarCanvas.d2y((state.day + 365 - getFirstDay()) % 365));
	}

}
