package kvv.heliostat.client;

import kvv.heliostat.client.panel.HorPanel;
import kvv.heliostat.client.panel.VertPanel;
import kvv.heliostat.shared.HeliostatState;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;

public class ControlView extends Composite implements View {

	private final Model model;

	private CheckBox clock = new CheckBox("Clock");
	private CheckBox shortDay = new CheckBox("5..19");

	private TextFieldView clockRate = new TextFieldView("Clock rate:", 0, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setClockRate(
					Integer.parseInt(text.getText()), new Callback<Void>());
		}
	};

	private WeatherView weatherTable;

	public ControlView(final Model model) {
		this.model = model;
		model.add(this);

		weatherTable = new WeatherView(model);

		clock.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.clock(clock.getValue(),
						new Callback<Void>());
			}
		});

		shortDay.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.shortDay(shortDay.getValue(),
						new Callback<Void>());
			}
		});

		SunPathView sunPathView = new SunPathView(model);

		Panel panel = new VertPanel(new HorPanel(true, 10, clock, shortDay,
				clockRate), weatherTable, new Gap(10, 10), new HorPanel(false,
				10, sunPathView, new MirrorView(model)));

		initWidget(panel);
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;
		clock.setValue(state.params.clock);
		shortDay.setValue(state.params.shortDay);

		if (!clockRate.focused)
			clockRate.text.setText("" + state.params.clockRate);

	}

}
