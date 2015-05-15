package kvv.heliostat.client;

import kvv.heliostat.client.panel.VertPanel;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.MotorState;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.spline.Function;
import kvv.heliostat.shared.spline.FunctionFactory;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

public class MotorsView extends Composite implements View {

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private final Model model;

	private int width = 400;
	private int height = 400;

	private Button clearHistory = new Button("Clear history",
			new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (Window.confirm("Clear history?"))
						model.heliostatService
								.clearHistory(new Callback<Void>());
				}
			});

	public MotorsView(final Model model) {
		this.model = model;
		model.add(this);
		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		canvas.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (model.lastState == null)
					return;
				MotorState[] state = model.lastState.motorState;
				if (state[0] == null || state[1] == null)
					return;
				if (state[0].max == null || state[1].max == null)
					return;
				if (!state[0].posValid || !state[1].posValid)
					return;

				int posX = (int) (0.5 + event.getX() * state[0].max / width);
				int posY = (int) (0.5 + (height - event.getY()) * state[1].max
						/ height);

				model.heliostatService.move(MotorId.AZ, posX,
						new Callback<Void>());
				model.heliostatService.move(MotorId.ALT, posY,
						new Callback<Void>());
			}
		});

		initWidget(new VertPanel(canvas, new Gap(8, 8), clearHistory));
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		context.setFillStyle("#808080");
		context.fillRect(0, 0, width, height);

		if (state.motorState[0] != null) {
			if (state.motorState[0].motorRawSimState != null) {
				context.beginPath();
				context.setLineWidth(4);
				context.setStrokeStyle("#C0C0C0");
				double x = (double) state.motorState[0].motorRawSimState.pos
						* width / state.motorState[0].motorRawSimState.max;
				context.moveTo(x, 0);
				context.lineTo(x, height);
				context.stroke();
				context.closePath();
			}
			if (state.motorState[0].posValid) {
				context.beginPath();
				context.setLineWidth(2);
				context.setStrokeStyle("#00FFFF");
				double x = (double) state.motorState[0].pos * width
						/ state.motorState[0].max;
				context.moveTo(x, 0);
				context.lineTo(x, height);
				context.stroke();
				context.closePath();
			}
		}

		if (state.motorState[1] != null) {
			if (state.motorState[1].motorRawSimState != null) {
				context.beginPath();
				context.setLineWidth(4);
				context.setStrokeStyle("#C0C0C0");
				double y = (double) height
						- state.motorState[1].motorRawSimState.pos * height
						/ state.motorState[1].motorRawSimState.max;
				context.moveTo(0, y);
				context.lineTo(width, y);
				context.stroke();
				context.closePath();
			}
			if (state.motorState[1].posValid) {
				context.beginPath();
				context.setLineWidth(2);
				context.setStrokeStyle("#00FFFF");
				double y = (double) height - state.motorState[1].pos * height
						/ state.motorState[1].max;
				context.moveTo(0, y);
				context.lineTo(width, y);
				context.stroke();
				context.closePath();
			}
		}

		context.beginPath();

		context.setStrokeStyle("#FFFF00");
		context.setLineWidth(2);

		boolean moved = false;

		for (double t = 5; t < 19; t += 0.1) {
			double alt = Environment.getMirrorAltitude(state.day, t);
			double az = Environment.getMirrorAzimuth(state.day, t);
			if (alt > 0) {
				double x1 = width * Environment.azDeg2Steps.value(az)
						/ Environment.MAX_STEPS;
				double y1 = height - height
						* Environment.altDeg2Steps.value(alt)
						/ Environment.MAX_STEPS;

				if (!moved)
					context.moveTo(x1, y1);
				else
					context.lineTo(x1, y1);
				moved = true;
			}
		}

		context.stroke();
		context.closePath();

		context.beginPath();
		context.setStrokeStyle("green");
		context.setFillStyle("green");

		if (state.trajectory != null && state.motorState[0].max != null
				&& state.motorState[1].max != null) {

			Function azFunc = FunctionFactory.getFunction(state.trajectory[0],
					state.trajectory[1]);
			Function altFunc = FunctionFactory.getFunction(state.trajectory[0],
					state.trajectory[2]);

			for (double t = 5; t <= 19; t += 0.1) {
				double x = width * azFunc.value(t) / state.motorState[0].max;
				double y = height - height * altFunc.value(t)
						/ state.motorState[1].max;
				if (t == 5)
					context.moveTo(x, y);
				else
					context.lineTo(x, y);
			}
			context.stroke();

			for (int i = 0; i < state.trajectory[0].length; i++) {
				double x1 = width * state.trajectory[1][i]
						/ state.motorState[0].max;
				double y1 = height - height * state.trajectory[2][i]
						/ state.motorState[1].max;

				context.fillRect(x1 - 2, y1 - 2, 4, 4);
			}
		}

		context.closePath();
	}

}
