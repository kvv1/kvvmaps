package kvv.heliostat.client.view;

import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.CaptPanel;
import kvv.gwtutils.client.Gap;
import kvv.gwtutils.client.HorPanel;
import kvv.gwtutils.client.VertPanel;
import kvv.gwtutils.client.login.LoginPanel;
import kvv.heliostat.client.dto.AutoMode;
import kvv.heliostat.client.dto.HeliostatState;
import kvv.heliostat.client.dto.MotorId;
import kvv.heliostat.client.model.Model;
import kvv.heliostat.client.model.View;
import kvv.heliostat.client.sim.ControlView;
import kvv.heliostat.shared.environment.Environment;
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

public class MainView extends Composite implements View {
	private RadioButton trackingManual = new RadioButton("trackingMode",
			"Manual");
	private RadioButton trackingSun = new RadioButton("trackingMode",
			"Sun only");
	private RadioButton trackingFull = new RadioButton("trackingMode", "Full");

	private Label date = new Label();
	private Label time = new Label();

	public MainView(final Model model) {
		model.add(this);

		trackingManual.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.OFF,
						new CallbackAdapter<Void>());
			}
		});

		trackingSun.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.SUN_ONLY,
						new CallbackAdapter<Void>());
			}
		});

		trackingFull.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				model.heliostatService.setAuto(AutoMode.FULL,
						new CallbackAdapter<Void>());
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

		Widget settingsPanel = new CaptPanel("Settings", new SettingsView(model));

		Widget dateTime = new HorPanel(false, 10, date, time);

		Widget azMotPanel = new CaptPanel("Azimuth motor", motorRawViewAz);
		Widget altMotPanel = new CaptPanel("Altitude motor", motorRawViewAlt);

		Widget motorsPanel = new CaptPanel("Motors", motorsView);
		Widget sensorPanel = new CaptPanel("Sensor", sensorView);

		TimeChart anglesChart = new AnglesChart(model);

		HorPanel motorsChart = new HorPanel(new MotorChartAz(model),  new Gap(6, 6),new MotorChartAlt(model));
		
//		MotorsChart motorsChart = new MotorsChart(model);

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

		Panel p = new HorPanel(new VertPanel(hp1, centralPanel, azMotPanel,
				altMotPanel, new Gap(6, 6), motorsChart, new Gap(6, 6),
				anglesChart), controlView);

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

		date.setText(state.dayTime.dayS);
		time.setText(state.dayTime.timeS);

	}

	static class MotorChartAz extends MotorChart {
		public MotorChartAz(Model model) {
			super(model, Environment.MIN_AZIMUTH, Environment.MAX_AZIMUTH, 10);
		}

		@Override
		public void updateView(HeliostatState state) {
			if (state == null)
				return;
			upd(state.azData, MirrorAngles.get(state.dayTime.day, state.dayTime.time).x);
		}
	}
	
	static class MotorChartAlt extends MotorChart {
		public MotorChartAlt(Model model) {
			super(model, Environment.MIN_ALTITUDE, Environment.MAX_ALTITUDE, 10);
		}

		@Override
		public void updateView(HeliostatState state) {
			if (state == null)
				return;
			upd(state.altData, MirrorAngles.get(state.dayTime.day, state.dayTime.time).y);
		}
	}
	
}
