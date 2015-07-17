package kvv.heliostat.client.chart;

import kvv.heliostat.client.Callback;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.client.HeliostatServiceAsync;
import kvv.heliostat.client.View;
import kvv.heliostat.shared.HeliostatState;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public class Chart1 extends Composite implements View {

	public final HeliostatServiceAsync heliostatService = GWT
			.create(HeliostatService.class);

	private final AbsolutePanel panel = new AbsolutePanel();

	private InnerCanvas[] canvas = new InnerCanvas[10];

	private final Canvas timeCanvas = Canvas.createIfSupported();
	private final Context2d timeContext = timeCanvas.getContext2d();

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

	public Chart1(final int width, int height, double minx, double maxx,
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

		ycanvas.setPixelSize(width + leftMargin, height + bottomMargin);
		ycanvas.setCoordinateSpaceWidth(width + leftMargin);
		ycanvas.setCoordinateSpaceHeight(height + bottomMargin);
		panel.add(ycanvas);
		panel.setWidgetPosition(ycanvas, 0, 0);

		panel.setPixelSize(width + leftMargin, height + bottomMargin);

		if (withTime) {
			timeCanvas.setPixelSize(2, height);
			timeCanvas.setCoordinateSpaceWidth(2);
			timeCanvas.setCoordinateSpaceHeight(height);

			timeContext.beginPath();
			timeContext.setStrokeStyle("yellow");
			timeContext.setLineWidth(2);
			timeContext.moveTo(1, 0);
			timeContext.lineTo(1, height);
			timeContext.stroke();
			timeContext.closePath();

			panel.add(timeCanvas);

		}

		draw();

		initWidget(panel);
	}

	public void set(int idx, ChartData data) {
		if (canvas[idx] != null)
			panel.remove(canvas[idx]);

		if (data != null) {
			canvas[idx] = new InnerCanvas(data);

			if (withTime) {
				canvas[idx].addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						int x = event.getX();
						heliostatService.setTime(x * 24.0 / width,
								new Callback<Void>());
					}
				});
			}
			panel.add(canvas[idx]);
			panel.setWidgetPosition(canvas[idx], leftMargin, 0);
		}
	}

	public void draw() {
		ycontext.beginPath();

		ycontext.setFillStyle("#808080");
		ycontext.fillRect(0, 0, ycanvas.getCoordinateSpaceWidth(),
				ycanvas.getCoordinateSpaceHeight());

		ycontext.setStrokeStyle("#404040");
		ycontext.setLineWidth(2);

		for (double arg = minx + stepx; arg < maxx; arg += stepx) {
			ycontext.moveTo(leftMargin + arg2x(arg), 0);
			ycontext.lineTo(leftMargin + arg2x(arg), height);
		}

		for (double val = miny + stepy; val < maxy; val += stepy) {
			ycontext.moveTo(leftMargin, val2y(val));
			ycontext.lineTo(leftMargin + width, val2y(val));
		}

		ycontext.stroke();
		ycontext.closePath();

		NumberFormat decimalFormat = NumberFormat.getFormat("#.##");

		ycontext.beginPath();
		ycontext.setFillStyle("yellow");
		for (double x = minx; x < maxx; x += stepx) {
			if (x != minx) {
				String text = decimalFormat.format(x);
				TextMetrics tm = ycontext.measureText(text);
				ycontext.fillText(text, leftMargin + arg2x(x) - tm.getWidth()
						/ 2, height + 10);
			}
		}
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

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		if (withTime)
			panel.setWidgetPosition(timeCanvas, (int) arg2x(state.time)
					+ leftMargin - 1, 0);
	}

	public double arg2x(double arg) {
		return width * (arg - minx) / (maxx - minx);
	}

	public double val2y(double val) {
		return height - height * (val - miny) / (maxy - miny);
	}

	private double x2arg(int x) {
		return minx + x * (maxx - minx) / width;
	}

	class InnerCanvas extends Composite {
		private final Canvas canvas = Canvas.createIfSupported();
		private final Context2d context = canvas.getContext2d();

		private final ChartData chartData;

		private InnerCanvas(ChartData chartData) {
			this.chartData = chartData;
			canvas.setPixelSize(width, height);
			canvas.setCoordinateSpaceWidth(width);
			canvas.setCoordinateSpaceHeight(height);
			initWidget(canvas);
			draw();
		}

		public void addMouseDownHandler(MouseDownHandler handler) {
			canvas.addMouseDownHandler(handler);
		}

		public void draw() {
			context.beginPath();
			context.setStrokeStyle(chartData.color);
			context.setLineWidth(2);

			if (chartData.function != null) {
				for (int x = 0; x <= width; x += 4) {
					double arg = x2arg(x);
					double val = chartData.function.value(arg);
					double y = val2y(val);

					if (x == 0)
						context.moveTo(x, y);
					else
						context.lineTo(x, y);
				}
			} else {
				for (int i = 0; i < chartData.xx.length; i++) {
					double x = arg2x(chartData.xx[i]);
					double y = val2y(chartData.yy[i]);

					if (i == 0)
						context.moveTo(x, y);
					else
						context.lineTo(x, y);
				}
			}

			context.stroke();

			context.setFillStyle("#FFFFFF");

			if (chartData.xx != null) {
				for (int i = 0; i < chartData.xx.length; i++) {
					double x = arg2x(chartData.xx[i]);
					double y = val2y(chartData.yy[i]);
					context.fillRect(x - 2, y - 2, 4, 4);
				}
			}

			context.closePath();
		}
	}
}
