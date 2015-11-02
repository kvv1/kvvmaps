package kvv.heliostat.client.view;

import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.HorPanel;
import kvv.gwtutils.client.TextFieldView;
import kvv.gwtutils.client.TextWithSaveButton;
import kvv.gwtutils.client.VertPanel;
import kvv.heliostat.client.Heliostat;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.Model.Callback1;
import kvv.heliostat.client.model.View;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class SettingsView extends Composite implements View {

	private TextFieldView stepsPerDegreeAz = new TextFieldView(
			"Azimuth steps/deg:", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setStepsPerDegree(MotorId.AZ,
					Integer.parseInt(text.getText()),
					new Callback1<Void>(model));
		}
	};

	private TextFieldView stepsPerDegreeAlt = new TextFieldView(
			"Altitude steps/deg:", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setStepsPerDegree(MotorId.ALT,
					Integer.parseInt(text.getText()),
					new Callback1<Void>(model));
		}
	};

	private TextFieldView stepRate = new TextFieldView("Algorithm step (ms):",
			120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setAlgorithmStepMS(
					Integer.parseInt(text.getText()),
					new Callback1<Void>(model));
		}
	};

	private TextFieldView azRange = new TextFieldView("Azimith range:", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setRange(MotorId.AZ,
					Integer.parseInt(text.getText()),
					new Callback1<Void>(model));
		}
	};

	private TextFieldView altRange = new TextFieldView("Altitude range:", 120,
			40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setRange(MotorId.ALT,
					Integer.parseInt(text.getText()),
					new Callback1<Void>(model));
		}
	};

	private CheckBox sim = new CheckBox("sim");
	{
		sim.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatServiceAux.setSim(sim.getValue(),
						new CallbackAdapter<Void>() {
							@Override
							public void onSuccess(Void result) {
								model.stop();
								Window.Location.reload();
							}
						});
			}
		});
	}

	private CheckBox clock = new CheckBox("Clock");
	{
		clock.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.clock(clock.getValue(),
						new Callback1<Void>(model));
			}
		});
	}

	private TextFieldView refreshPeriod = new TextFieldView("Refresh (ms):",
			120, 40) {

		String speriod = Cookies.getCookie(Heliostat.REFRESH_PERIOD);
		{
			text.setText(speriod);
		}

		@Override
		protected void onClick(ClickEvent event) {
			try {
				int period = Integer.parseInt(text.getText());
				if (period < 0 || period > 10000)
					return;
				speriod = text.getText();
				Cookies.setCookie(Heliostat.REFRESH_PERIOD, speriod);
				model.stop();
				model.start();
			} finally {
				text.setText(speriod);
			}

			Cookies.setCookie(Heliostat.REFRESH_PERIOD, speriod);
		}
	};

	private TextWithSaveButton controllerParamsPanel = new TextWithSaveButton(
			"Controller settings", "100%", "100px") {
		@Override
		protected void save(String text, final AsyncCallback<Void> callback) {
			AsyncCallback<Void> callback2 = new Callback1<Void>(model) {
				public void onSuccess(Void result) {
					super.onSuccess(result);
					callback.onSuccess(result);
				}

				public void onFailure(Throwable caught) {
					super.onFailure(caught);
					callback.onFailure(caught);
				}
			};

			model.heliostatService.setControllerParams(text, callback2);
		}
	};

	private final Model model;

	private VertPanel panel;

	public SettingsView(Model model) {
		this.model = model;
		model.add(this);

		panel = new VertPanel(HasHorizontalAlignment.ALIGN_RIGHT, new HorPanel(
				true, 10, sim, clock), stepsPerDegreeAz, stepsPerDegreeAlt,
				stepRate, azRange, altRange, refreshPeriod,
				controllerParamsPanel);

		initWidget(panel);
	}

	@Override
	public void updateView(HeliostatState state) {
		if (!stepsPerDegreeAz.focused)
			stepsPerDegreeAz.text.setText("" + state.params.stepsPerDegree[0]);

		if (!stepsPerDegreeAlt.focused)
			stepsPerDegreeAlt.text.setText("" + state.params.stepsPerDegree[1]);

		if (!stepRate.focused)
			stepRate.text.setText("" + state.params.stepMS);

		if (!azRange.focused)
			azRange.text.setText("" + state.params.range[0]);

		if (!altRange.focused)
			altRange.text.setText("" + state.params.range[1]);

		if (!controllerParamsPanel.focused)
			controllerParamsPanel.setText(state.params.controllerParams);

		sim.setValue(state.params.SIM);
		clock.setValue(state.params.clock);
	}
}
