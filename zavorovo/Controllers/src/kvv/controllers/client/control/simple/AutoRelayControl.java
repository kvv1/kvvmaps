package kvv.controllers.client.control.simple;

import java.util.ArrayList;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.ChildComposite;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.history.shared.HistoryItem;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AutoRelayControl extends ControlComposite {

	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	private final HorizontalPanel framePanel = new HorizontalPanel();
	private final HorizontalPanel horizontalPanel = new HorizontalPanel();
	private final HorizontalPanel schedulePanel = new HorizontalPanel();
	private final ScheduleCanvas scheduleCanvas;

	private final CheckBox autoButton;
	private final ChildComposite relayControl;

	public final RegisterDescr reg;

	public AutoRelayControl(final RegisterDescr reg, RegisterPresentation presentation,
			MouseMoveHandler mouseMoveHandler) {
		this.reg = reg;

		scheduleCanvas = new ScheduleCanvas(reg, presentation, mouseMoveHandler) {
			public void save(String regName,
					final RegisterSchedule registerSchedule) {
				scheduleService.update(regName, registerSchedule,
						new CallbackAdapter<Void>() {
							public void onSuccess(Void result) {
								refreshButtons(registerSchedule);
							};
						});
			}
		};

		// framePanel.setBorderWidth(1);
		framePanel.add(horizontalPanel);

		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.add(schedulePanel);
		enableSchedule(true);

		autoButton = new CheckBox("Авто");

		if (presentation.isBool()) {
			relayControl = new SimpleRelayControl(reg.addr, reg.register, "");
		} else {
			relayControl = new GetRegControl(reg.addr, reg.register, 1, null);
		}
		add(relayControl);

		autoButton.setValue(false);
		autoButton.setEnabled(false);

		VerticalPanel panel = new VerticalPanel();
		//panel.setBorderWidth(1);
		VerticalPanel labelPanel = new VerticalPanel();
		labelPanel.setWidth("200px");
		labelPanel.add(new Label(reg.name));
		panel.add(labelPanel);
		HorizontalPanel panel1 = new HorizontalPanel();
		//panel1.setBorderWidth(1);
		// panel1.setWidth("200px");
		panel1.add(relayControl);
		panel1.add(autoButton);
		panel1.setCellWidth(relayControl, "40px");
		panel.add(panel1);
		horizontalPanel.add(panel);

		autoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.check()) {
					autoButton.setValue(!autoButton.getValue());
					return;
				}

				final boolean auto = autoButton.getValue();
				scheduleService.enable(reg.name, auto,
						new CallbackAdapter<Void>() {
							@Override
							public void onSuccess(Void result) {
								relayControl.setEnabled(!auto);
							}
						});
			}
		});

		initWidget(framePanel);
	}

	private void refreshButtons(RegisterSchedule registerSchedule) {
		if (registerSchedule != null) {
			autoButton.setEnabled(true);
			autoButton.setValue(registerSchedule.enabled);
			relayControl.setEnabled(!registerSchedule.enabled);
		} else {
			autoButton.setEnabled(false);
			autoButton.setValue(false);
			relayControl.setEnabled(true);
		}
	}

	public void refreshSchedule(RegisterSchedule registerSchedule,
			ArrayList<HistoryItem> logItems, int markerSeconds,
			int historyEndSeconds) {
		refreshButtons(registerSchedule);
		scheduleCanvas.refresh(registerSchedule, logItems, markerSeconds,
				historyEndSeconds);
	}

	public void enableSchedule(boolean value) {
		schedulePanel.clear();
		if (value)
			schedulePanel.add(scheduleCanvas);
	}

	public void drawMarker(int x) {
		scheduleCanvas.drawMarker(x);
	}

}
