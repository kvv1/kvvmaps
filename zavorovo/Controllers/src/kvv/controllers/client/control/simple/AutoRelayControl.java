package kvv.controllers.client.control.simple;

import java.util.ArrayList;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.AllRegs;
import kvv.controllers.client.control.ChildComposite;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.shared.HistoryItem;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.State;
import kvv.gwtutils.client.CallbackAdapter;
import kvv.gwtutils.client.HorPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.CheckBox;
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

	private final CheckBox autoCB = new CheckBox("Расп.");
	private final CheckBox exprCB = new CheckBox();

	private final ExprButton exprButton;
	private final ChildComposite relayControl;

	private RegisterSchedule registerSchedule;

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

		scheduleCanvas = new ScheduleCanvas(presentation, mouseMoveHandler,
				60 * 24 * 6 / 10, 0, 60 * 24, 10, false) {
			public void save(final RegisterSchedule registerSchedule,
					String comment) {
				saveSched(registerSchedule, comment);
			}
		};

		// framePanel.setBorderWidth(1);
		framePanel.add(horizontalPanel);

		// horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.add(schedulePanel);
		enableSchedule(true);

		if (presentation.isBool()) {
			relayControl = new SimpleRelayControl(reg.controllerAddr,
					reg.register, "");
		} else {
			relayControl = new GetRegControl(reg.controllerAddr, reg.register,
					null);
		}
		add(relayControl);

		autoCB.setValue(false);
		autoCB.setEnabled(false);
		autoCB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (registerSchedule == null)
					return;

				RegisterSchedule rs = new RegisterSchedule(registerSchedule);

				rs.state = autoCB.getValue() ? State.SCHEDULE : State.MANUAL;
				saveSched(rs, "state := " + rs.state);
			}
		});

		exprCB.setValue(false);
		exprCB.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (registerSchedule == null)
					return;

				RegisterSchedule rs = new RegisterSchedule(registerSchedule);

				rs.state = exprCB.getValue() ? State.EXPRESSION : State.MANUAL;
				saveSched(rs, "state := " + rs.state);
			}
		});

		exprButton = new ExprButton(reg) {
			@Override
			public void save(RegisterSchedule registerSchedule) {
				saveSched(registerSchedule, "выражения");
			}
		};

		VerticalPanel panel = new VerticalPanel();
		// panel.setBorderWidth(1);
		VerticalPanel labelPanel = new VerticalPanel();
		labelPanel.setWidth("200px");
		labelPanel.add(new Label(reg.name));
		panel.add(labelPanel);
		HorPanel panel1 = new HorPanel(false, 8, relayControl, autoCB, exprCB,
				exprButton);
		// panel1.setCellWidth(relayControl, "40px");
		panel.add(panel1);
		horizontalPanel.add(panel);

		initWidget(framePanel);
	}

	private void saveSched(final RegisterSchedule registerSchedule,
			String comment) {
		scheduleService.saveSchedule(reg.name, registerSchedule, comment,
				new CallbackAdapter<Void>() {
					public void onSuccess(Void result) {
						AutoRelayControl.this.registerSchedule = registerSchedule;
						refreshButtons();
						scheduleCanvas
								.refresh(AutoRelayControl.this.registerSchedule);
					};

					@Override
					public void onFailure(Throwable caught) {
						refreshButtons();
						scheduleCanvas
								.refresh(AutoRelayControl.this.registerSchedule);
					}
				});
	}

	private void refreshButtons() {
		if (registerSchedule != null && registerSchedule.items.size() != 0) {
			autoCB.setEnabled(true);
			autoCB.setValue(registerSchedule.state == State.SCHEDULE);
		} else {
			autoCB.setEnabled(false);
			autoCB.setValue(false);
		}

		exprCB.setValue(registerSchedule.state == State.EXPRESSION);

		exprButton.updateUI(registerSchedule);
		relayControl.setEnabled(registerSchedule.state == State.MANUAL);
	}

	public void refreshSchedule(RegisterSchedule registerSchedule,
			ArrayList<HistoryItem> logItems, int markerSeconds) {

		this.registerSchedule = registerSchedule;
		refreshButtons();
		scheduleCanvas.refresh(registerSchedule, logItems, markerSeconds);
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
