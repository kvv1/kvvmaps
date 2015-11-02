package kvv.heliostat.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kvv.gwtutils.client.CallbackAdapter;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.HeliostatService;
import kvv.heliostat.client.HeliostatServiceAsync;
import kvv.heliostat.client.HeliostatServiceAux;
import kvv.heliostat.client.HeliostatServiceAuxAsync;
import kvv.heliostat.client.dto.HeliostatState;

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

	public List<View> added = new ArrayList<>();
	public List<View> removed = new ArrayList<>();

	public static class Callback1<T> extends CallbackAdapter<T> {
		private final Model model;

		public Callback1(Model model) {
			this.model = model;
		}

		@Override
		public void onSuccess(T result) {
			super.onSuccess(result);
			model.notifyViews();
		}
	}

	public void notifyViews() {
		for (View view : views)
			view.updateView(null);
		start();
	}

	private void updt(HeliostatState state) {
		views.removeAll(removed);
		views.addAll(added);
		removed.clear();
		added.clear();
		for (View view : views)
			view.updateView(state);
	}

	private void err(Throwable caught) {
		views.removeAll(removed);
		views.addAll(added);
		removed.clear();
		added.clear();
		for (View view : views)
			if (view instanceof ErrHandler)
				((ErrHandler) view).onError(caught);
	}

	public void add(View view) {
		added.add(view);
	}

	public void remove(View view) {
		removed.add(view);
	}

	public int getPeriod() {
		String speriod = Cookies.getCookie(Heliostat.REFRESH_PERIOD);
		if (speriod == null) {
			speriod = "1000";
			Cookies.setCookie(Heliostat.REFRESH_PERIOD, speriod);
		}
		int period = Integer.parseInt(speriod);
		return period;
	}

	private Timer timer = new Timer() {
		private int reqNo;

		@Override
		public void run() {
			heliostatService.getState(++reqNo,
					new AsyncCallback<HeliostatState>() {

						@Override
						public void onFailure(Throwable caught) {
							lastState = null;
							err(caught);
							schedule(getPeriod());
						}

						@Override
						public void onSuccess(HeliostatState result) {
							if (reqNo == result.reqNo) {
								lastState = result;
								if (updates)
									updt(result);
							}
							schedule(getPeriod());
						}
					});
		}
	};

	public void start() {
		timer.schedule(10);
	}

	public void stop() {
		timer.cancel();
	}

	public Model() {
	}

}
