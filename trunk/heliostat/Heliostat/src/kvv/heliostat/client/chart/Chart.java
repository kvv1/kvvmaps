package kvv.heliostat.client.chart;

import kvv.heliostat.client.Callback;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.client.HeliostatServiceAsync;
import kvv.heliostat.client.View;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.spline.Function;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public class Chart extends Composite implements View {

	public final HeliostatServiceAsync heliostatService = GWT
			.create(HeliostatService.class);

	private final AbsolutePanel panel = new AbsolutePanel();

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private final Canvas timeCanvas = Canvas.createIfSupported();
	private final Context2d timeContext = timeCanvas.getContext2d();

	private final Canvas xcanvas = Canvas.createIfSupported();
	private final Context2d xcontext = xcanvas.getContext2d();

	private final Canvas ycanvas = Canvas.createIfSupported();
	private final Context2d ycontext = ycanvas.getContext2d();

	private static final int leftMargin = 30;
	private static final int bottomMargin = 12;

	private final int width;
	private final int height;
	private final double minx;
	private final double maxx;
	private final double miny;
	private final double maxy;
	private final double stepx;
	private final double stepy;

	private final boolean withTime;

	public Chart(final int width, int height, double minx, double maxx,
			double stepx, double miny, double maxy, double stepy,
			boolean withTime) {
		this.width = width;
		this.height = height;
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
		this.stepx = stepx;
		this.stepy = stepy;
		this.withTime = withTime;

		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		panel.add(canvas);
		panel.setWidgetPosition(canvas, leftMargin, 0);

		xcanvas.setPixelSize(width, bottomMargin);
		xcanvas.setCoordinateSpaceWidth(width);
		xcanvas.setCoordinateSpaceHeight(bottomMargin);
		panel.add(xcanvas);
		panel.setWidgetPosition(xcanvas, leftMargin, height);

		ycanvas.setPixelSize(leftMargin, height + bottomMargin);
		ycanvas.setCoordinateSpaceWidth(leftMargin);
		ycanvas.setCoordinateSpaceHeight(height + bottomMargin);
		panel.add(ycanvas);
		panel.setWidgetPosition(ycanvas, 0, 0);

		panel.setPixelSize(width + leftMargin, height + bottomMargin);

		if (withTime) {
			timeCanvas.setPixelSize(2, height);
			timeCanvas.setCoordinateSpaceWidth(2);
			timeCanvas.setCoordinateSpaceHeight(height);

			timeContext.beginPath();
			timeContext.setFillStyle("yellow");
			timeContext.fillRect(0, 0, 2, height);
			timeContext.closePath();

			panel.add(timeCanvas);

			canvas.addMouseDownHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					int x = event.getX();
					heliostatService.setTime(x * 24.0 / width,
							new Callback<Void>());
				}
			});
		}

		initWidget(panel);
	}

	public static class ChartData {

		public final Function function;
		public final double[] xx;
		public final double[] yy;
		public final String color;

		public ChartData(Function function, String color) {
			this.function = function;
			this.xx = null;
			this.yy = null;
			this.color = color;
		}

		public ChartData(double[] xx, double[] yy, String color) {
			this.function = null;
			this.xx = xx;
			this.yy = yy;
			this.color = color;
		}

	}

	private ChartData[] data;

	public void set(ChartData... data) {
		this.data = data;
		draw();
	}

	public void draw() {
		drawBG();
		if (data != null) {

			for (ChartData d : data) {
				context.beginPath();
				context.setStrokeStyle(d.color);
				context.setLineWidth(2);

				if (d.function != null) {
					for (int x = 0; x <= width; x++) {
						double arg = x2arg(x);
						double val = d.function.value(arg);
						double y = val2y(val);

						if (x == 0)
							context.moveTo(x, y);
						else
							context.lineTo(x, y);
					}
				} else {
					for (int i = 0; i < d.xx.length; i++) {
						double x = arg2x(d.xx[i]);
						double y = val2y(d.yy[i]);

						if (i == 0)
							context.moveTo(x, y);
						else
							context.lineTo(x, y);
					}
				}

				context.stroke();

				context.setFillStyle("#FFFFFF");

				if (d.xx != null) {
					for (int i = 0; i < d.xx.length; i++) {
						double x = arg2x(d.xx[i]);
						double y = val2y(d.yy[i]);
						context.fillRect(x - 2, y - 2, 4, 4);
					}
				}

				context.closePath();
			}
		}

	}

	private void drawBG() {
		NumberFormat decimalFormat = NumberFormat.getFormat("#.##");

		context.beginPath();

		context.setFillStyle("#808080");
		context.fillRect(0, 0, width, height);

		context.setStrokeStyle("#404040");
		context.setLineWidth(2);

		for (double arg = minx + stepx; arg < maxx; arg += stepx) {
			context.moveTo(arg2x(arg), 0);
			context.lineTo(arg2x(arg), height);
		}

		for (double val = miny + stepy; val < maxy; val += stepy) {
			context.moveTo(0, val2y(val));
			context.lineTo(width, val2y(val));
		}

		context.stroke();
		context.closePath();

		xcontext.beginPath();
		xcontext.setFillStyle("#808080");
		xcontext.fillRect(0, 0, xcanvas.getCoordinateSpaceWidth(),
				xcanvas.getCoordinateSpaceHeight());
		xcontext.setFillStyle("yellow");
		for (double x = minx; x < maxx; x += stepx) {
			if (x != minx) {
				String text = decimalFormat.format(x);
				TextMetrics tm = xcontext.measureText(text);
				xcontext.fillText(text, arg2x(x) - tm.getWidth() / 2, 10);
			}
		}

		xcontext.closePath();

		ycontext.beginPath();
		ycontext.setFillStyle("#808080");
		ycontext.fillRect(0, 0, ycanvas.getCoordinateSpaceWidth(),
				ycanvas.getCoordinateSpaceHeight());
		ycontext.setFillStyle("yellow");
		for (double y = miny; y < maxy; y += stepy) {
			if (y != miny) {
				String text = decimalFormat.format(y);
				TextMetrics tm = ycontext.measureText(text);
				ycontext.fillText(text, leftMargin - tm.getWidth() - 2,
						val2y(y) + 2);
			}
		}

		ycontext.closePath();
	}

	private double arg2x(double arg) {
		return width * (arg - minx) / (maxx - minx);
	}

	private double val2y(double val) {
		return height - height * (val - miny) / (maxy - miny);
	}

	private double x2arg(int x) {
		return minx + x * (maxx - minx) / width;
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		if (withTime)
			panel.setWidgetPosition(timeCanvas, (int) arg2x(state.time)
					+ leftMargin, 0);
	}

}
