package kvv.heliostat.client.view;

import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.Gap;
import kvv.gwtutils.client.VertPanel;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.dto.MotorState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.math.MirrorAngles;
import kvv.simpleutils.spline.Function;
import kvv.simpleutils.spline.FunctionFactory;

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

	private int width = 250;
	private int height = 250;

	private Button clearHistory = new Button("Clear history",
			new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (Window.confirm("Clear history?"))
						model.heliostatService
								.clearHistory(new CallbackAdapter<Void>());
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

				int azRange = model.lastState.params.range[0];
				int altRange = model.lastState.params.range[1];

				MotorState[] state = model.lastState.motorState;
				if (state[0] == null || state[1] == null)
					return;
				if (!state[0].posValid || !state[1].posValid)
					return;

				int posX = (int) (0.5 + event.getX() * azRange / width);
				int posY = (int) (0.5 + (height - event.getY()) * altRange
						/ height);

				model.heliostatService.move(MotorId.AZ, posX,
						new CallbackAdapter<Void>());
				model.heliostatService.move(MotorId.ALT, posY,
						new CallbackAdapter<Void>());
			}
		});

		initWidget(new VertPanel(canvas, new Gap(4, 4), clearHistory));
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		context.setFillStyle("gray");
		context.fillRect(0, 0, width, height);

		int azRange = model.lastState.params.range[0];
		int altRange = model.lastState.params.range[1];

		if (state.motorState[0] != null) {
			if (state.motorState[0].posValid) {
				context.beginPath();
				context.setLineWidth(2);
				context.setStrokeStyle(Heliostat.AZ_COLOR);
				double x = (double) state.motorState[0].pos * width / azRange;
				context.moveTo(x, 0);
				context.lineTo(x, height);
				context.stroke();
				context.closePath();
			} else if (state.motorState[0].posAbs != null) {
				context.beginPath();
				context.setLineWidth(2);
				context.setStrokeStyle("#C0C0C0");
				double x = (double) state.motorState[0].posAbs * width
						/ state.params.range[0];
				context.moveTo(x, 0);
				context.lineTo(x, height);
				context.stroke();
				context.closePath();
			}
		}

		if (state.motorState[1] != null) {
			if (state.motorState[1].posValid) {
				context.beginPath();
				context.setLineWidth(2);
				context.setStrokeStyle(Heliostat.ALT_COLOR);
				double y = height - (double) state.motorState[1].pos * height
						/ altRange;
				context.moveTo(0, y);
				context.lineTo(width, y);
				context.stroke();
				context.closePath();
			} else if (state.motorState[1].posAbs != null) {
				context.beginPath();
				context.setLineWidth(2);
				context.setStrokeStyle("#C0C0C0");
				double y = height - (double) state.motorState[1].posAbs
						* height / state.params.range[1];
				context.moveTo(0, y);
				context.lineTo(width, y);
				context.stroke();
				context.closePath();
			}
		}

		{

			if (state.altData != null && state.altData != null) {
				Function azFunc = FunctionFactory.getFunction(state.azData[0],
						state.azData[1]);
				Function altFunc = FunctionFactory.getFunction(
						state.altData[0], state.altData[1]);

				if (azFunc != null && altFunc != null) {
					String styleOld = null;

					context.beginPath();
					context.setLineWidth(2);

					for (double t = 5; t <= 19; t += 0.1) {
						String style = Heliostat.TRAJ_COLOR;

						double az = MirrorAngles.get(state.day, t).x;
						double alt = MirrorAngles.get(state.day, t).y;

						double azSteps = azFunc.value(az);
						double altSteps = altFunc.value(alt);

						boolean azFound = false;
						for (double az1 : state.azData[0]) {
							if (Math.abs(az - az1) < Environment.ANGLE_STEP) {
								azFound = true;
								break;
							}
						}

						boolean altFound = false;
						for (double alt1 : state.altData[0]) {
							if (Math.abs(alt - alt1) < Environment.ANGLE_STEP) {
								altFound = true;
								break;
							}
						}

						if (azFound && altFound)
							style = Heliostat.TRAJ_COLOR_LIGHT_LIGHT;

						double x = width * azSteps / azRange;
						double y = height - height * altSteps / altRange;

						if (!style.equals(styleOld)) {
							if (styleOld != null)
								context.lineTo(x, y);
							context.stroke();
							context.closePath();
							context.beginPath();
							styleOld = style;
							context.setStrokeStyle(styleOld);
							context.moveTo(x, y);
						}

						context.lineTo(x, y);

					}
					context.stroke();
					context.closePath();

					context.beginPath();
					context.setStrokeStyle("yellow");
					context.setFillStyle("yellow");
					{
						double az = MirrorAngles.get(state.day, state.time).x;
						double alt = MirrorAngles.get(state.day, state.time).y;

						double azSteps = azFunc.value(az);
						double altSteps = altFunc.value(alt);

						double x = width * azSteps / azRange;
						double y = height - height * altSteps / altRange;

						int rad = 3;
						context.arc(x, y, rad, 0, Math.PI * 2.0, true);
						if (state.sensorState.isValid())
							context.fill();
						context.stroke();
					}
					context.closePath();

				}
			}
		}
	}

}
