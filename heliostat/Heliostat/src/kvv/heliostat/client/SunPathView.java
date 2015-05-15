package kvv.heliostat.client;

import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.environment.Environment;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public class SunPathView extends Composite implements View {

	private AbsolutePanel panel = new AbsolutePanel();

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private final Model model;

	private int width = 360;
	private int height = 360;

	private Integer day;

	public SunPathView(Model model) {
		this.model = model;
		model.add(this);

		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		panel.add(canvas);

		context.beginPath();
		context.setFillStyle("gray");
		context.fillRect(0, 0, canvas.getCoordinateSpaceWidth(),
				canvas.getCoordinateSpaceHeight());
		context.closePath();

		initWidget(panel);
	}

	void draw() {

	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		context.beginPath();
		context.setFillStyle("gray");
		context.fillRect(0, 0, canvas.getCoordinateSpaceWidth(),
				canvas.getCoordinateSpaceHeight());
		context.closePath();

		int rad = 5;

		double az = Environment.getMirrorAzimuth(state.day, state.time);
		double alt = Environment.getMirrorAltitude(state.day, state.time);

		double x = (az - Environment.MIN_AZIMUTH) * width
				/ (Environment.MAX_AZIMUTH - Environment.MIN_AZIMUTH);

		double y = height - (alt - Environment.MIN_ALTITUDE) * height
				/ (Environment.MAX_ALTITUDE - Environment.MIN_ALTITUDE);

		context.beginPath();
		context.setFillStyle(state.sun ? "yellow" : "light-gray");
		context.arc(x, y, rad, 0, Math.PI * 2.0, true);
		context.fill();

		context.setStrokeStyle("yellow");
		for (float t = 5; t <= 19; t += 0.1f) {
			double az1 = Environment.getMirrorAzimuth(state.day, t);
			double alt1 = Environment.getMirrorAltitude(state.day, t);

			double x1 = (az1 - Environment.MIN_AZIMUTH) * width
					/ (Environment.MAX_AZIMUTH - Environment.MIN_AZIMUTH);

			double y1 = height - (alt1 - Environment.MIN_ALTITUDE) * height
					/ (Environment.MAX_ALTITUDE - Environment.MIN_ALTITUDE);

			if (t == 5)
				context.moveTo(x1, y1);
			else
				context.lineTo(x1, y1);
		}
		context.stroke();

		context.closePath();

	}

}
