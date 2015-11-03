package kvv.heliostat.client.view;

import kvv.gwtutils.client.CaptPanel;
import kvv.gwtutils.client.Gap;
import kvv.gwtutils.client.HorPanel;
import kvv.gwtutils.client.VertPanel;
import kvv.gwtutils.client.login.LoginPanel;
import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.model.ErrHandler;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.Model.Callback1;
import kvv.heliostat.client.model.View;
import kvv.heliostat.client.sim.ControlView;
import kvv.heliostat.shared.math.MirrorAngles;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class MainView extends Composite implements View, ErrHandler {
	private Label error = new Label();
	private RadioButton trackingManual = new RadioButton("trackingMode",
			"Manual");
	private RadioButton trackingSun = new RadioButton("trackingMode",
			"Sun only");
	private RadioButton trackingFull = new RadioButton("trackingMode", "Full");

	private Label date = new Label();
	private Label time = new Label();

	private HorPanel motorsChart = new HorPanel();
	private MotorChartAz motorChartAz;
	private MotorChartAlt motorChartAlt;

	private HorPanel controlPanel = new HorPanel();

	private final Model model;

	public MainView(final Model model) {
		this.model = model;

		model.add(this);

		trackingManual.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.OFF,
						new Callback1<Void>(model));
			}
		});

		trackingSun.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.SUN_ONLY,
						new Callback1<Void>(model));
			}
		});

		trackingFull.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.FULL,
						new Callback1<Void>(model));
			}
		});

		MotorsView motorsView = new MotorsView(model);
		model.add(motorsView);

		SensorView sensorView = new SensorView(model);
		model.add(sensorView);

		// ControlView controlView = new ControlView(model);

		MotorRawView motorRawViewAz = new MotorRawView(model, MotorId.AZ);

		MotorRawView motorRawViewAlt = new MotorRawView(model, MotorId.ALT);

		Widget autoPanel = new CaptPanel("Tracking mode", new VertPanel(
				trackingManual, trackingSun, trackingFull));

		Widget settingsPanel = new CaptPanel("Settings",
				new SettingsView(model));

		Widget dateTime = new HorPanel(false, 10, date, time);

		Widget azMotPanel = new CaptPanel("Azimuth motor", motorRawViewAz);
		Widget altMotPanel = new CaptPanel("Altitude motor", motorRawViewAlt);

		Widget motorsPanel = new CaptPanel("Motors", motorsView);
		Widget sensorPanel = new CaptPanel("Sensor", sensorView);

		TimeChart anglesChart = new AnglesChart(model);

		motorChartAz = new MotorChartAz(model, 1000);
		motorChartAlt = new MotorChartAlt(model, 1000);
		motorsChart.add(motorChartAz, new Gap(6, 6), motorChartAlt);

		Widget centralPanel = new HorPanel(
				new VertPanel(autoPanel, sensorPanel), motorsPanel,
				settingsPanel);

		LoginPanel loginPanel = new LoginPanel();

		HorizontalPanel hp1 = new HorizontalPanel();
		hp1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		hp1.add(dateTime);
		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		hp1.add(loginPanel);
		hp1.setWidth("100%");

		Panel p = new HorPanel(new VertPanel(error, hp1, centralPanel,
				azMotPanel, altMotPanel, new Gap(6, 6), motorsChart, new Gap(6,
						6), anglesChart), controlPanel);

		// Panel p = new HorPanel(new VertPanel(dateTime, topPanel, azMotPanel,
		// altMotPanel, new HorPanel(motorsPanel, sensorPanel), new Gap(6,
		// 6), anglesChart), controlView);

		initWidget(p);
	}

	@Override
	public void updateView(HeliostatState state) {
		error.setVisible(false);

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

		date.setText(state.dayTime.dayS);
		time.setText(state.dayTime.timeS);

		if (state.params.SIM && controlPanel.getWidgetCount() == 0)
			controlPanel.add(new ControlView(model));
	}

	@Override
	public void onError(Throwable caught) {
		error.getElement().getStyle().setColor("red");
		error.setVisible(true);
		error.setText(caught.getClass().getSimpleName() + " "
				+ caught.getMessage());
	}

	static class MotorChartAz extends MotorChart {
		public MotorChartAz(Model model, int maxy) {
			super(model, -10, 10, 10, maxy);
		}

		@Override
		public void updateView(HeliostatState state) {
			upd(state.azData,
					MirrorAngles.get(state.dayTime.day, state.dayTime.time).x);
			setMaxY(state.params.range[0]);
			setMinX(state.params.simParams.MIN_AZIMUTH);
			setMaxX(state.params.simParams.MAX_AZIMUTH);
		}
	}

	static class MotorChartAlt extends MotorChart {
		public MotorChartAlt(Model model, int maxy) {
			super(model, -10, 10, 10, maxy);
		}

		@Override
		public void updateView(HeliostatState state) {
			upd(state.altData,
					MirrorAngles.get(state.dayTime.day, state.dayTime.time).y);
			setMaxY(state.params.range[1]);
			setMinX(state.params.simParams.MIN_ALTITUDE);
			setMaxX(state.params.simParams.MAX_ALTITUDE);
		}
	}

}
