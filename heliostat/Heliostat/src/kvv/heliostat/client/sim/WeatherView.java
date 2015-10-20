package kvv.heliostat.client.sim;

import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.TextFieldView;
import kvv.gwtutils.client.VertPanel;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.Weather;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;

public class WeatherView extends Composite implements View {

	private CalendarView calendarView;

	private final CheckBox editMode = new CheckBox("Edit mode");

	private Weather weather;

	private Boolean val;

	private final TextFieldView day;

	private AsyncCallback<Weather> weatherCallback = new AsyncCallback<Weather>() {
		@Override
		public void onSuccess(Weather result) {
			setWeather(result);
		}

		@Override
		public void onFailure(Throwable caught) {
			setWeather(null);
		}
	};

	public WeatherView(final Model model) {

		day = new TextFieldView("Day:", 0, 40) {
			@Override
			protected void onClick(ClickEvent event) {
				model.heliostatService.resetSim(
						Integer.parseInt(text.getText()), weatherCallback);
			}
		};

		final Timer timer = new Timer() {
			@Override
			public void run() {
				model.updates = true;
				model.heliostatService.saveWeather(weather,
						new CallbackAdapter<Void>());
			}
		};

		calendarView = new CalendarView(model) {
			@Override
			protected void drawContent() {
				calendarView.calendarCanvas.context.beginPath();
				calendarView.calendarCanvas.context.setFillStyle("yellow");

				for (int d = 0; d < weather.values.length; d++) {
					for (int p = 0; p < weather.getPts(); p++) {
						if (weather.values[d][p]) {
							double x1 = calendarView.calendarCanvas.t2x(weather
									.p2t(p));
							double x2 = calendarView.calendarCanvas.t2x(weather
									.p2t(p + 1));

							calendarView.calendarCanvas.context.fillRect(x1
									- (x2 - x1) / 2,
									calendarView.calendarCanvas.d2y(d),
									x2 - x1,
									calendarView.calendarCanvas.dayHeight);
						}
					}
				}

				calendarView.calendarCanvas.context.closePath();
			}

			@Override
			protected int getFirstDay() {
				if (weather != null)
					return weather.firstDay;
				return 0;
			}

			@Override
			protected void onMouseDown(int x, int y, int dayOffset, double time) {
				if (weather == null)
					return;

				double t = calendarView.calendarCanvas.x2t(x);
				if (t < calendarView.calendarCanvas.sunrise
						|| t > calendarView.calendarCanvas.sunset)
					return;

				if (dayOffset > weather.values.length)
					return;

				if (editMode.getValue()) {
					int p = weather.t2p(t);
					if (p < 0 || p >= weather.getPts())
						return;

					weather.values[dayOffset][p] = !weather.values[dayOffset][p];

					val = weather.values[dayOffset][p];

					model.updates = false;
					timer.schedule(3000);

					calendarView.draw();
				} else {
					model.heliostatService.setTime(t, new CallbackAdapter<Void>());
					model.heliostatService.setDay(
							(dayOffset + getFirstDay()) % 365,
							new CallbackAdapter<Void>());
				}

			}

			@Override
			protected void onMouseUp(int x, int y) {
				val = null;
			}

			@Override
			protected void onMouseMove(int x, int y) {
				if (weather == null)
					return;

				if (val == null)
					return;

				double t = calendarView.calendarCanvas.x2t(x);
				if (t < calendarView.calendarCanvas.sunrise
						|| t > calendarView.calendarCanvas.sunset)
					return;

				int d = calendarView.calendarCanvas.y2d(y);
				if (d < 0 || d >= calendarView.calendarCanvas.days
						|| d > weather.values.length)
					return;

				int p = weather.t2p(t);
				if (p < 0 || p >= weather.getPts())
					return;

				weather.values[d][p] = val;

				model.updates = false;
				timer.schedule(3000);

				calendarView.draw();
			}

			@Override
			protected void onTimeClicked(double time) {
				model.heliostatService.setTime(time, new CallbackAdapter<Void>());
			}

			@Override
			protected void onDayClicked(int day) {
				model.heliostatService.setDay(day, new CallbackAdapter<Void>());
			}
		};

		model.add(this);

		model.heliostatService.getWeather(weatherCallback);

		initWidget(new VertPanel(day, editMode, calendarView));
	}

	private void setWeather(Weather result) {
		weather = result;
		calendarView.draw();
		day.text.setText("" + result.firstDay);
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;
	}
}
