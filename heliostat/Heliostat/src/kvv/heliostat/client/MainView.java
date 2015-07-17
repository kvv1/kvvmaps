package kvv.heliostat.client;

import kvv.heliostat.client.chart.Chart;
import kvv.heliostat.client.panel.CaptPanel;
import kvv.heliostat.client.panel.HorPanel;
import kvv.heliostat.client.panel.VertPanel;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.Params.AutoMode;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class MainView extends Composite implements View {
	private final Model model;

	private RadioButton trackingManual = new RadioButton("trackingMode",
			"Manual");
	private RadioButton trackingSun = new RadioButton("trackingMode",
			"Sun only");
	private RadioButton trackingFull = new RadioButton("trackingMode", "Full");

	private Label date = new Label();
	private Label time = new Label();

	private TextFieldView stepsPerDegreeAz = new TextFieldView(
			"Azimuth steps/deg:", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setStepsPerDegree(MotorId.AZ,
					Integer.parseInt(text.getText()), new Callback<Void>());
		}
	};

	private TextFieldView stepsPerDegreeAlt = new TextFieldView(
			"Altitude steps/deg:", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setStepsPerDegree(MotorId.ALT,
					Integer.parseInt(text.getText()), new Callback<Void>());
		}
	};

	private TextFieldView stepRate = new TextFieldView("Algorithm step (ms):",
			120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setStepMS(Integer.parseInt(text.getText()),
					new Callback<Void>());
		}
	};

	private TextFieldView azRange = new TextFieldView("Azimith range:", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setRange(MotorId.AZ,
					Integer.parseInt(text.getText()), new Callback<Void>());
		}
	};

	private TextFieldView altRange = new TextFieldView("Altitude range:", 120,
			40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setRange(MotorId.ALT,
					Integer.parseInt(text.getText()), new Callback<Void>());
		}
	};

	private Button azCalibr = new Button("Calibrate azimuth",
			new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					model.heliostatService.calibrate(MotorId.AZ,
							new Callback<Void>());
				}
			});

	private Button altCalibr = new Button("Calibrate altitude",
			new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					model.heliostatService.calibrate(MotorId.ALT,
							new Callback<Void>());
				}
			});

	public MainView(final Model model) {
		this.model = model;

		model.add(this);

		trackingManual.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.OFF,
						new Callback<Void>());
			}
		});

		trackingSun.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.SUN_ONLY,
						new Callback<Void>());
			}
		});

		trackingFull.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.FULL,
						new Callback<Void>());
			}
		});

		MotorsView motorsView = new MotorsView(model);
		model.add(motorsView);

		SensorView sensorView = new SensorView(model);
		model.add(sensorView);

		ControlView controlView = new ControlView(model);

		MotorRawView motorRawViewAz = new MotorRawView(model, MotorId.AZ);

		MotorRawView motorRawViewAlt = new MotorRawView(model, MotorId.ALT);

		Widget autoPanel = new CaptPanel("Tracking mode", new VertPanel(
				trackingManual, trackingSun, trackingFull));

		Widget settingsPanel = new CaptPanel("Settings", new VertPanel(
				HasHorizontalAlignment.ALIGN_RIGHT, stepsPerDegreeAz,
				stepsPerDegreeAlt, stepRate, azRange, azCalibr, altRange,
				altCalibr));

		Widget dateTime = new HorPanel(false, 10, date, time);

		Widget azMotPanel = new CaptPanel("Azimuth motor", motorRawViewAz);
		Widget altMotPanel = new CaptPanel("Altitude motor", motorRawViewAlt);

		Widget motorsPanel = new CaptPanel("Motors", motorsView);
		Widget sensorPanel = new CaptPanel("Sensor", sensorView);

		Chart anglesChart = new AnglesChart();
		model.add(anglesChart);

		Chart motorsChart = new MotorsChart();
		model.add(motorsChart);

		Widget centralPanel = new HorPanel(
				new VertPanel(autoPanel, sensorPanel), motorsPanel,
				settingsPanel);

		Panel p = new HorPanel(new VertPanel(dateTime, centralPanel,
				azMotPanel, altMotPanel, new Gap(6, 6), motorsChart, new Gap(6,
						6), anglesChart), controlView);

		// Panel p = new HorPanel(new VertPanel(dateTime, topPanel, azMotPanel,
		// altMotPanel, new HorPanel(motorsPanel, sensorPanel), new Gap(6,
		// 6), anglesChart), controlView);

		initWidget(p);
	}

	@Override
	public void updateView(HeliostatState state) {
		if (state == null)
			return;
		switch (state.params.auto) {
		case OFF:
			trackingManual.setValue(true);
			break;
		case SUN_ONLY:
			trackingSun.setValue(true);
			break;
		case FULL:
			trackingFull.setValue(true);
			break;
		default:
			break;
		}

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

		date.setText(state.dayS);
		time.setText(state.timeS);
	}

}
