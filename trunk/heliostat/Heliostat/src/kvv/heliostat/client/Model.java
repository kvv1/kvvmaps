package kvv.heliostat.client;

import java.util.ArrayList;
import java.util.Collection;

import kvv.heliostat.shared.HeliostatState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Model {

	public final HeliostatServiceAsync heliostatService = GWT
			.create(HeliostatService.class);

	private Collection<View> views = new ArrayList<>();

	public HeliostatState lastState;

	protected boolean updates = true;

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

	public Model() {
		Timer timer = new Timer() {

			@Override
			public void run() {
				heliostatService.getState(new AsyncCallback<HeliostatState>() {

					@Override
					public void onFailure(Throwable caught) {
						lastState = null;
						if (updates)
							for (View view : views)
								view.updateView(null);
						schedule(200);
					}

					@Override
					public void onSuccess(HeliostatState result) {
						lastState = result;
						if (updates)
							for (View view : views)
								view.updateView(result);

						schedule(200);
					}
				});
			}
		};

		timer.schedule(200);
	}

}
