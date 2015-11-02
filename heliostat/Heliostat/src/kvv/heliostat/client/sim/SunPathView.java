package kvv.heliostat.client.sim;

import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.Model.Callback1;
import kvv.heliostat.client.model.View;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.spline.Function;
import kvv.simpleutils.spline.FunctionFactory;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;

public class SunPathView extends Composite implements View {

	private AbsolutePanel panel = new AbsolutePanel();

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private int width = 250;
	private int height = 250;

	public SunPathView(final Model model) {
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

		canvas.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (azDeg2Steps == null)
					return;

				double az = x2az(0.5 + event.getX());
				double alt = y2alt(0.5 + event.getY());

				int xPos = (int) azDeg2Steps.value(az);
				int yPos = (int) altDeg2Steps.value(alt);

				model.heliostatService.move(MotorId.AZ, xPos,
						new Callback1<Void>(model));

				model.heliostatService.move(MotorId.ALT, yPos,
						new Callback1<Void>(model));
			}
		});

		initWidget(panel);
	}

	void draw() {

	}

	HeliostatState state;
	Function azDeg2Steps;
	Function altDeg2Steps;

	@Override
	public void updateView(HeliostatState state) {
		this.state = state;
		azDeg2Steps = null;
		altDeg2Steps = null;

		azDeg2Steps = FunctionFactory.getFunction(
				state.params.simParams.azDeg2Steps[0],
				state.params.simParams.azDeg2Steps[1]);
		altDeg2Steps = FunctionFactory.getFunction(
				state.params.simParams.altDeg2Steps[0],
				state.params.simParams.altDeg2Steps[1]);

		context.beginPath();
		context.setFillStyle("gray");
		context.fillRect(0, 0, canvas.getCoordinateSpaceWidth(),
				canvas.getCoordinateSpaceHeight());
		context.closePath();

		int rad = 5;

		double az = MirrorAngles.get(state.dayTime.day, state.dayTime.time).x;
		double alt = MirrorAngles.get(state.dayTime.day, state.dayTime.time).y;

		double x = az2x(az);
		double y = alt2y(alt);

		context.beginPath();
		context.setFillStyle(state.sensorState.isValid() ? "yellow"
				: "light-gray");
		context.arc(x, y, rad, 0, Math.PI * 2.0, true);
		context.fill();

		context.setStrokeStyle("yellow");
		for (float t = 5; t <= 19; t += 0.1f) {
			double az1 = MirrorAngles.get(state.dayTime.day, (double) t).x;
			double alt1 = MirrorAngles.get(state.dayTime.day, (double) t).y;

			double x1 = az2x(az1);
			double y1 = alt2y(alt1);

			if (t == 5)
				context.moveTo(x1, y1);
			else
				context.lineTo(x1, y1);
		}
		context.stroke();

		context.closePath();

		if (state.motorState[0].posValid && state.motorState[1].posValid) {
			context.beginPath();
			context.setStrokeStyle("black");

			double motorAz = FunctionFactory.solve(azDeg2Steps,
					state.motorState[0].posAbs,
					state.params.simParams.MIN_AZIMUTH,
					state.params.simParams.MAX_AZIMUTH, 0.01);
			double motorAlt = FunctionFactory.solve(altDeg2Steps,
					state.motorState[1].posAbs,
					state.params.simParams.MIN_ALTITUDE,
					state.params.simParams.MAX_ALTITUDE, 0.01);

			double x1 = az2x(motorAz);
			double y1 = alt2y(motorAlt);

			context.moveTo(x1 - 10, y1);
			context.lineTo(x1 + 10, y1);

			context.moveTo(x1, y1 - 10);
			context.lineTo(x1, y1 + 10);

			context.stroke();
			context.closePath();
		}
	}

	private double az2x(double az) {
		return (az - state.params.simParams.MIN_AZIMUTH)
				* width
				/ (state.params.simParams.MAX_AZIMUTH - state.params.simParams.MIN_AZIMUTH);
	}

	private double x2az(double x) {
		return state.params.simParams.MIN_AZIMUTH
				+ (state.params.simParams.MAX_AZIMUTH - state.params.simParams.MIN_AZIMUTH)
				* x / width;
	}

	private double alt2y(double alt) {
		return height
				- (alt - state.params.simParams.MIN_ALTITUDE)
				* height
				/ (state.params.simParams.MAX_ALTITUDE - state.params.simParams.MIN_ALTITUDE);
	}

	private double y2alt(double y) {
		return state.params.simParams.MIN_ALTITUDE
				+ (state.params.simParams.MAX_ALTITUDE - state.params.simParams.MIN_ALTITUDE)
				* (height - y) / height;
	}

}
