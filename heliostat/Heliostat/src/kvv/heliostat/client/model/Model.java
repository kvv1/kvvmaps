package kvv.heliostat.client.model;

import java.util.ArrayList;
import java.util.Collection;

import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.client.HeliostatServiceAsync;
import kvv.heliostat.client.HeliostatServiceAux;
import kvv.heliostat.client.HeliostatServiceAuxAsync;
import kvv.heliostat.shared.HeliostatState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Model {

	public final HeliostatServiceAsync heliostatService = GWT
			.create(HeliostatService.class);
	
	public final HeliostatServiceAuxAsync heliostatServiceAux = GWT
			.create(HeliostatServiceAux.class);

	private Collection<View> views = new ArrayList<>();

	public HeliostatState lastState;

	public boolean updates = true;

	public void notifyViews() {

		for (View view : views)
			view.updateView(null);

		heliostatService.getState(new AsyncCallback<HeliostatState>() {

			@Override
			public void onFailure(Throwable caught) {
				lastState = null;
			}

			@Override
			public void onSuccess(HeliostatState result) {
				lastState = result;
				for (View view : views)
					view.updateView(result);
			}
		});
	}

	public void add(View view) {
		views.add(view);
	}

	private Timer timer;

	public int getPeriod() {
		String speriod = Cookies.getCookie(Heliostat.REFRESH_PERIOD);
		if(speriod == null) {
			speriod = "1000";
			Cookies.setCookie(Heliostat.REFRESH_PERIOD, speriod);
		}
		int period = Integer.parseInt(speriod);
		return period;
	}
	
	public void start() {
		if (timer != null)
			return;

		
		timer = new Timer() {
			@Override
			public void run() {
				heliostatService.getState(new AsyncCallback<HeliostatState>() {

					@Override
					public void onFailure(Throwable caught) {
						lastState = null;
						if (updates)
							for (View view : views)
								view.updateView(null);
						schedule(getPeriod());
					}

					@Override
					public void onSuccess(HeliostatState result) {
						lastState = result;
						if (updates)
							for (View view : views)
								view.updateView(result);

						schedule(getPeriod());
					}
				});
			}
		};

		timer.schedule(getPeriod());
	}

	public void stop() {
		if(timer == null)
			return;
		timer.cancel();
		timer = null;
	}

	public Model() {
	}

}
