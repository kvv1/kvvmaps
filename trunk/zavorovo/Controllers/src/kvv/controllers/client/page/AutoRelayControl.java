package kvv.controllers.client.page;

import java.util.Date;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.client.control.simple.SimpleRelayControl;
import kvv.controllers.shared.Register;
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
	private final SimpleRelayControl relayControl;

	final Register reg;

	public AutoRelayControl(int addr, final Register reg,
			MouseMoveHandler mouseMoveHandler) {
		super(addr);
		this.reg = reg;

		scheduleCanvas = new ScheduleCanvas(mouseMoveHandler);

		// framePanel.setBorderWidth(1);
		framePanel.add(horizontalPanel);

		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.add(schedulePanel);

		autoButton = new CheckBox("Авто");
		relayControl = new SimpleRelayControl(addr, reg.register, "");
		add(relayControl);

		autoButton.setValue(false);
		autoButton.setEnabled(false);

		VerticalPanel panel = new VerticalPanel();
		VerticalPanel labelPanel = new VerticalPanel();
		labelPanel.setWidth("200px");
		labelPanel.add(new Label(reg.name));
		panel.add(labelPanel);
		HorizontalPanel panel1 = new HorizontalPanel();
		panel1.setWidth("100%");
		panel1.add(autoButton);
		panel1.add(relayControl);
		panel.add(panel1);
		horizontalPanel.add(panel);

		ClickHandler clickHandler = new ClickHandler() {
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
		};

		autoButton.addClickHandler(clickHandler);

		initWidget(framePanel);
	}

	public void refreshSchedule(RegisterSchedule registerSchedule, Date date) {
		autoButton.setEnabled(true);
		scheduleCanvas.refresh(registerSchedule.items, date);
		autoButton.setValue(registerSchedule.enabled);
		relayControl.setEnabled(!registerSchedule.enabled);
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
