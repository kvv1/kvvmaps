package kvv.heliostat.client.chart;

import kvv.heliostat.client.Callback;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.client.HeliostatServiceAsync;
import kvv.heliostat.client.Model;
import kvv.heliostat.client.View;
import kvv.heliostat.shared.HeliostatState;

import com.google.gwt.core.client.GWT;

public class TimeChart extends Chart implements View {

	public final HeliostatServiceAsync heliostatService = GWT
			.create(HeliostatService.class);

	public TimeChart(Model model, int width, int height, double minx,
			double maxx, double stepx, double miny, double maxy, double stepy,
			double[] solidVals) {
		super(width, height, minx, maxx, stepx, miny, maxy, stepy, solidVals);
		model.add(this);
	}

	@Override
	protected void onClick(double arg) {
		heliostatService.setTime(arg, new Callback<Void>());
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;

		setCursor(state.time);
	}

}
