package kvv.gwtutils.client.chart;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

public class Chart extends Composite {

	private final AbsolutePanel panel = new AbsolutePanel();

	private Widget[] layers = new Widget[10];

	private final Canvas timeCanvas = Canvas.createIfSupported();
	private final Context2d timeContext = timeCanvas.getContext2d();

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private final AbsolutePanel layersPanel = new AbsolutePanel();

	private static final int leftMargin = 34;
	private static final int bottomMargin = 12;

	private final int width;
	private final int height;
	private final double minx;
	private final double maxx;
	private final double miny;
	private final double maxy;
	private final double stepx;
	private final double stepy;
	private final double[] solidVals;

	public Chart(final int width, int height, double minx, double maxx,
			double stepx, double miny, double maxy, double stepy,
			double[] solidVals) {
		this.width = width;
		this.height = height;
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
		this.stepx = stepx;
		this.stepy = stepy;
		this.solidVals = solidVals;

		panel.setPixelSize(width + leftMargin, height + bottomMargin);

		canvas.setPixelSize(width + leftMargin, height + bottomMargin);
		canvas.setCoordinateSpaceWidth(width + leftMargin);
		canvas.setCoordinateSpaceHeight(height + bottomMargin);
		panel.add(canvas);
		panel.setWidgetPosition(canvas, 0, 0);

		layersPanel.setPixelSize(width, height);
		panel.add(layersPanel);
		panel.setWidgetPosition(layersPanel, leftMargin, 0);

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

		panel.setWidgetPosition(timeCanvas, -10, 0);

		FocusPanel w = new FocusPanel();
		w.setPixelSize(width, height);
		panel.add(w);
		panel.setWidgetPosition(w, leftMargin, 0);
		w.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				onClick(x2arg(event.getX()));
			}
		});

		draw();

		initWidget(panel);
	}

	protected void onClick(double arg) {
	}

	public void set(int idx, Widget w) {
		if (layers[idx] != null)
			layersPanel.remove(layers[idx]);

		layers[idx] = null;

		if (w != null) {
			layers[idx] = w;
			layersPanel.add(w);
			layersPanel.setWidgetPosition(w, 0, 0);
		}
	}

	public void set(int idx, ChartData chartData) {
		if (chartData == null) {
			set(idx, (Widget) null);
			return;
		}

		Canvas canvas = createCanvas();
		Context2d context = canvas.getContext2d();
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

		if (chartData.mark != null) {
//			context.setStrokeStyle(chartData.color);
//			context.setLineWidth(1);
//			
//			double x = arg2x(chartData.mark);
//			double val = chartData.function.value(chartData.mark);
//			double y = val2y(val);
//
//			context.moveTo(x - 10, y);
//			context.lineTo(x + 10, y);
//
//			context.moveTo(x, y - 10);
//			context.lineTo(x, y + 10);
//			context.stroke();
		}

		context.setFillStyle("#FFFFFF");

		if (chartData.xx != null) {
			for (int i = 0; i < chartData.xx.length; i++) {
				double x = arg2x(chartData.xx[i]);
				double y = val2y(chartData.yy[i]);
				context.fillRect(x - 2, y - 2, 4, 4);
			}
		}


		context.closePath();

		set(idx, canvas);
	}

	public Canvas createCanvas() {
		Canvas canvas = Canvas.createIfSupported();
		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		return canvas;
	}

	public void draw() {
		context.setFillStyle("#808080");
		context.fillRect(0, 0, canvas.getCoordinateSpaceWidth(),
				canvas.getCoordinateSpaceHeight());

		context.setStrokeStyle("#404040");

		context.beginPath();
		context.setLineWidth(2);

		for (double arg = minx + stepx; arg < maxx; arg += stepx) {
			context.moveTo(leftMargin + arg2x(arg), 0);
			context.lineTo(leftMargin + arg2x(arg), height);
		}

		for (double val = miny + stepy; val < maxy; val += stepy) {
			context.moveTo(leftMargin, val2y(val));
			context.lineTo(leftMargin + width, val2y(val));
		}

		context.stroke();
		context.closePath();

		if (solidVals != null) {
			context.beginPath();
			context.setStrokeStyle("black");

			for (double val : solidVals) {
				context.moveTo(leftMargin, val2y(val));
				context.lineTo(leftMargin + width, val2y(val));
			}

			context.stroke();
			context.closePath();
		}

		NumberFormat decimalFormat = NumberFormat.getFormat("#.##");

		context.beginPath();
		context.setFillStyle("yellow");
		for (double x = minx; x < maxx; x += stepx) {
			if (x != minx) {
				String text = decimalFormat.format(x);
				TextMetrics tm = context.measureText(text);
				context.fillText(text, leftMargin + arg2x(x) - tm.getWidth()
						/ 2, height + 10);
			}
		}
		for (double y = miny; y < maxy; y += stepy) {
			if (y != miny) {
				String text = decimalFormat.format(y);
				TextMetrics tm = context.measureText(text);
				context.fillText(text, leftMargin - tm.getWidth() - 2,
						val2y(y) + 2);
			}
		}

		context.closePath();
	}

	public void setCursor(double arg) {
		panel.setWidgetPosition(timeCanvas, (int) arg2x(arg) + leftMargin - 1,
				0);
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

}
