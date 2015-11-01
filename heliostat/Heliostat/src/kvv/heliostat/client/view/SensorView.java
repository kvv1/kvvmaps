package kvv.heliostat.client.view;

import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.SensorState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;

public class SensorView extends Composite implements View {

	private final Canvas canvas = Canvas.createIfSupported();
	private final Context2d context = canvas.getContext2d();

	private int width = 200;
	private int height = 200;

	public SensorView(Model model) {
		canvas.setPixelSize(width, height);
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);

		model.add(this);

		initWidget(canvas);
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		context.beginPath();

		context.setFillStyle("#808080");
		context.fillRect(0, 0, width, height);

		context.setStrokeStyle("black");
		context.setLineWidth(1);

		context.moveTo(width / 2, 0);
		context.lineTo(width / 2, height);

		context.moveTo(0, height / 2);
		context.lineTo(width, height / 2);

		context.stroke();

		context.setStrokeStyle("#404040");
		context.moveTo(width / 2 - 20, 0);
		context.lineTo(width / 2 - 20, height);
		context.moveTo(width / 2 + 20, 0);
		context.lineTo(width / 2 + 20, height);
		context.moveTo(0, height / 2 - 20);
		context.lineTo(width, height / 2 - 20);
		context.moveTo(0, height / 2 + 20);
		context.lineTo(width, height / 2 + 20);

		context.stroke();

		SensorState ss = state.sensorState;

		if (ss == null) {
			context.setFillStyle("red");
			context.fillRect(width / 2 - 2, height / 2 - 2, 4, 4);
		} else {
			// if (ss.getDeflection() != null) {
			double x = ss.getDeflectionX() * 20;
			double y = ss.getDeflectionY() * 20;

			context.setFillStyle(ss.isValid() ? "yellow" : "#C0C0C0");

			context.beginPath();
			context.arc(width / 2 + x, height / 2 - y, 20, 0, Math.PI * 2.0,
					true);
			context.closePath();
			context.fill();
			// }

			context.setFillStyle("yellow");

			context.fillText(ss.tl + "", 2, 10);
			context.fillText(ss.bl + "", 2, height - 2);

			context.fillText(ss.tr + "", width
					- context.measureText(ss.tr + "").getWidth() - 2, 10);
			context.fillText(ss.br + "", width
					- context.measureText(ss.br + "").getWidth() - 2,
					height - 2);

			// if (ss.getDeflection() != null) {
			NumberFormat decimalFormat = NumberFormat.getFormat("#.##");

			String dx = decimalFormat.format(ss.getDeflectionX());
			String dy = decimalFormat.format(ss.getDeflectionY());
			context.fillText(dx, 40, 40);
			context.fillText(dy, 40, 55);
			context.fillText("t=" + ss.temperature, 40, 70);
			// }
		}

		context.closePath();

	}

}
