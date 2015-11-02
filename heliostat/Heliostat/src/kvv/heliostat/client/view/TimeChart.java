package kvv.heliostat.client.view;

import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.chart.Chart;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;

public class TimeChart extends Chart implements View {

	private final Model model;

	public TimeChart(Model model, int width, int height, double minx,
			double maxx, double stepx, double miny, double maxy, double stepy,
			double[] solidVals) {
		super(width, height, minx, maxx, stepx, miny, maxy, stepy, solidVals);
		this.model = model;
		model.add(this);
	}

	@Override
	protected void onClick(double arg) {
		model.heliostatServiceAux.setTime(arg, new CallbackAdapter<Void>());
	}

	@Override
	public void updateView(HeliostatState state) {
		setCursor(state.dayTime.time);
	}

}
