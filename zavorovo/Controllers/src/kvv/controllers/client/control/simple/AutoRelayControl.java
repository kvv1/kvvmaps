package kvv.controllers.client.control.simple;

import java.util.ArrayList;

import kvv.controller.register.AllRegs;
import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.ChildComposite;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.history.shared.HistoryItem;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.State;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseMoveHandler;
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

	private final AutoButton autoButton;
	private final ExprButton exprButton;
	private final ChildComposite relayControl;

	public final RegisterDescr reg;

	@Override
	public void refresh() {
		super.refresh();
	}

	@Override
	public void refresh(AllRegs result) {
		super.refresh(result);
	}

	public AutoRelayControl(final RegisterDescr reg,
			RegisterPresentation presentation, MouseMoveHandler mouseMoveHandler) {
		this.reg = reg;

		scheduleCanvas = new ScheduleCanvas(presentation, mouseMoveHandler) {
			public void save(final RegisterSchedule registerSchedule) {
				scheduleService.update(reg.name, registerSchedule,
						new CallbackAdapter<RegisterSchedule>() {
							public void onSuccess(RegisterSchedule result) {
								refreshButtons(result);
							};
						});
			}
		};

		// framePanel.setBorderWidth(1);
		framePanel.add(horizontalPanel);

		//horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.add(schedulePanel);
		enableSchedule(true);

		if (presentation.isBool()) {
			relayControl = new SimpleRelayControl(reg.addr, reg.register, "");
		} else {
			relayControl = new GetRegControl(reg.addr, reg.register, 1, null);
		}
		add(relayControl);

		autoButton = new AutoButton() {
			@Override
			public void save(RegisterSchedule registerSchedule) {
				scheduleService.update(reg.name, registerSchedule,
						new CallbackAdapter<RegisterSchedule>() {
							public void onSuccess(RegisterSchedule result) {
								refreshButtons(result);
							};
						});
			}
		};

		exprButton = new ExprButton() {
			@Override
			public void save(RegisterSchedule registerSchedule) {
				scheduleService.update(reg.name, registerSchedule,
						new CallbackAdapter<RegisterSchedule>() {
							public void onSuccess(RegisterSchedule result) {
								refreshButtons(result);
							};
						});
			}
		};

		VerticalPanel panel = new VerticalPanel();
		// panel.setBorderWidth(1);
		VerticalPanel labelPanel = new VerticalPanel();
		labelPanel.setWidth("200px");
		labelPanel.add(new Label(reg.name));
		panel.add(labelPanel);
		HorizontalPanel panel1 = new HorizontalPanel();
		// panel1.setBorderWidth(1);
		// panel1.setWidth("200px");
		panel1.add(relayControl);
		panel1.add(autoButton);
		panel1.add(exprButton);

		panel1.setCellWidth(relayControl, "40px");
		panel.add(panel1);
		horizontalPanel.add(panel);

		initWidget(framePanel);
	}

	private void refreshButtons(RegisterSchedule registerSchedule) {
		autoButton.updateUI(registerSchedule);
		exprButton.updateUI(registerSchedule);
		relayControl.setEnabled(registerSchedule.state == State.MANUAL);
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
