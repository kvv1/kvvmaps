package kvv.heliostat.client.sim;

import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;
import kvv.heliostat.shared.math.Matrix3x3;
import kvv.simpleutils.spline.Function;
import kvv.simpleutils.spline.FunctionFactory;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.client.ui.Composite;

public class MirrorView extends Composite implements View {

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private int width = 200;
	private int height = 200;

	public MirrorView(Model model) {
		model.add(this);

		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		initWidget(canvas);
	}

	private void draw() {
		context.beginPath();

		context.setFillStyle("#F0F0F0");
		context.fillRect(0, 0, width, height);

		context.setFillStyle("gray");

		context.setStrokeStyle("gray");
		context.setLineWidth(2);
		Point p = getPoint(matrix.apply(-1.1, 0, 0));
		context.moveTo(p.getX(), p.getY());
		p = getPoint(matrix.apply(1.1, 0, 0));
		context.lineTo(p.getX(), p.getY());
		context.stroke();

		context.closePath();

		context.beginPath();
		context.setFillStyle("gray");

		p = getPoint(matrix.apply(-1, 0, -1));
		context.moveTo(p.getX(), p.getY());

		p = getPoint(matrix.apply(1, 0, -1));
		context.lineTo(p.getX(), p.getY());

		p = getPoint(matrix.apply(1, 0, 1));
		context.lineTo(p.getX(), p.getY());

		p = getPoint(matrix.apply(-1, 0, 1));
		context.lineTo(p.getX(), p.getY());

		context.fill();

		context.closePath();
	}

	private Point getPoint(double x, double y, double z) {
		return new Point(4 * x * width / (z + 10) + width / 2, height / 2 - 4
				* y * height / (z + 10));
	}

	private Point getPoint(double[] v) {
		return getPoint(v[0], v[1], v[2]);
	}

	Matrix3x3 matrix = new Matrix3x3();

	void rect(String color, int x, int y, int w, int h) {

	}

	@Override
	public void updateView(HeliostatState state) {

		if (state.motorState[0] == null || state.motorState[1] == null)
			return;

		Function azDeg2Steps = FunctionFactory.getFunction(
				state.params.simParams.azDeg2Steps[0],
				state.params.simParams.azDeg2Steps[1]);
		Function altDeg2Steps = FunctionFactory.getFunction(
				state.params.simParams.altDeg2Steps[0],
				state.params.simParams.altDeg2Steps[1]);

		double motorAz = FunctionFactory.solve(azDeg2Steps,
				state.motorState[0].posAbs, state.params.simParams.MIN_AZIMUTH,
				state.params.simParams.MAX_AZIMUTH, 0.01);
		double motorAlt = FunctionFactory.solve(altDeg2Steps,
				state.motorState[1].posAbs,
				state.params.simParams.MIN_ALTITUDE,
				state.params.simParams.MAX_ALTITUDE, 0.01);

		matrix = (Matrix3x3) Matrix3x3.rotZ(-motorAz * Math.PI / 180).mul(
				Matrix3x3.rotX(-motorAlt * Math.PI / 180), null);

		draw();

	}
}
