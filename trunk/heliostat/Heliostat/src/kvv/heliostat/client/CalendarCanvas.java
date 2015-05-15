package kvv.heliostat.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;

public class CalendarCanvas {
	protected final Canvas canvas = Canvas.createIfSupported();
	protected final Context2d context = canvas.getContext2d();

	protected int days = 30;
	protected int sunrise = 5;
	protected int sunset = 19;

	private static int hourWidth = 40;
	public static int dayHeight = 10;

	public int width = hourWidth * (sunset - sunrise);
	public int height = dayHeight * days;

	public CalendarCanvas() {
		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
	}

	public void clearGrid() {
		context.beginPath();
		context.setFillStyle("gray");
		context.fillRect(0, 0, width, height);
		context.closePath();
	}

	protected void drawGrid() {
		context.beginPath();

		context.setStrokeStyle("dark-gray");
		context.setLineWidth(0.5);
		for (int t = sunrise; t <= sunset; t++) {
			context.moveTo(t2x(t), 0);
			context.lineTo(t2x(t), height);
		}

		for (int d = 0; d <= days; d++) {
			context.moveTo(0, d2y(d));
			context.lineTo(width, d2y(d));
		}

		context.stroke();
		context.closePath();
	}

	public double t2x(double t) {
		return width * (t - sunrise) / (sunset - sunrise);
	}

	public int d2y(int d) {
		return d * dayHeight;
	}

	public double x2t(int x) {
		return sunrise + (double) x * (sunset - sunrise) / width;
	}

	public int y2d(int y) {
		return y * days / height;
	}

}
