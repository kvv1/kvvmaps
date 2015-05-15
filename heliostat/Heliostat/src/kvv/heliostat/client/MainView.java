package kvv.heliostat.client;

import kvv.heliostat.client.chart.Chart;
import kvv.heliostat.client.chart.Chart.ChartData;
import kvv.heliostat.client.panel.CaptPanel;
import kvv.heliostat.client.panel.HorPanel;
import kvv.heliostat.client.panel.VertPanel;
import kvv.heliostat.shared.HeliostatState;
import kvv.heliostat.shared.MotorId;
import kvv.heliostat.shared.Params.AutoMode;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.spline.Function;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class MainView extends Composite implements View {
	private final Model model;

	private RadioButton trackingManual = new RadioButton("trackingMode", "Manual");
	private RadioButton trackingSun = new RadioButton("trackingMode", "Sun only");
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

	private TextFieldView stepRate = new TextFieldView("Algorithm step (ms):", 120, 40) {
		@Override
		protected void onClick(ClickEvent event) {
			model.heliostatService.setStepMS(Integer.parseInt(text.getText()),
					new Callback<Void>());
		}
	};

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

		Widget autoPanel = new CaptPanel("Tracking mode", new VertPanel(trackingManual,
				trackingSun, trackingFull));

		Widget settingsPanel = new CaptPanel("Settings", new VertPanel(
				stepsPerDegreeAz, stepsPerDegreeAlt, stepRate));

		Widget dateTime = new HorPanel(false, 10, date, time);

		Widget azMotPanel = new CaptPanel("Azimuth motor", motorRawViewAz);
		Widget altMotPanel = new CaptPanel("Altitude motor", motorRawViewAlt);

		Widget motorsPanel = new CaptPanel("Motors", motorsView);
		Widget sensorPanel = new CaptPanel("Sensor", sensorView);

		Chart anglesChart = new Chart(600, 200, 0, 24, 1, -60, 60, 10, true);
		model.add(anglesChart);

		Function az = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAzimuth(0, t);
			}
		};

		Function az1 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAzimuth(91, t);
			}
		};

		Function az2 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAzimuth(182, t);
			}
		};

		Function alt = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAltitude(0, t);
			}
		};

		Function alt1 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAltitude(91, t);
			}
		};

		Function alt2 = new Function() {
			@Override
			public double value(double t) {
				return Environment.getMirrorAltitude(182, t);
			}
		};

		anglesChart.set(new ChartData(az, "cyan"), new ChartData(az1, "cyan"),
				new ChartData(az2, "cyan"), new ChartData(alt, "LawnGreen"),
				new ChartData(alt1, "LawnGreen"), new ChartData(alt2,
						"LawnGreen"));

		Widget centralPanel = new HorPanel(new VertPanel(autoPanel,
				settingsPanel, sensorPanel), motorsPanel);

		Panel p = new HorPanel(new VertPanel(dateTime, centralPanel,
				azMotPanel, altMotPanel, new Gap(6, 6), anglesChart),
				controlView);

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

		date.setText(state.dayS);
		time.setText(state.timeS);
	}

}
