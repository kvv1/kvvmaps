package kvv.controllers.client.page;

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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AutoRelayControl extends ControlComposite {

	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	HorizontalPanel framePanel = new HorizontalPanel();
	HorizontalPanel horizontalPanel = new HorizontalPanel();
	HorizontalPanel schedulePanel = new HorizontalPanel();
	ScheduleCanvas scheduleCanvas = new ScheduleCanvas();

	RadioButton autoButton;
	RadioButton manualButton;
	SimpleRelayControl relayControl;

	Register reg;

	public AutoRelayControl(int addr, final Register reg) {
		super(addr);
		this.reg = reg;

		//framePanel.setBorderWidth(1);
		framePanel.add(horizontalPanel);

		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.add(schedulePanel);

		autoButton = new RadioButton("reg" + reg.name, "Авто");
		manualButton = new RadioButton("reg" + reg.name, "Ручн.");
		relayControl = new SimpleRelayControl(addr, reg.register, "");

		autoButton.setValue(false);
		manualButton.setValue(true);
		autoButton.setEnabled(false);
		manualButton.setEnabled(false);

		VerticalPanel panel = new VerticalPanel();
		VerticalPanel labelPanel = new VerticalPanel();
		labelPanel.setWidth("200px");
		labelPanel.add(new Label(reg.name));
		panel.add(labelPanel);
		HorizontalPanel panel1 = new HorizontalPanel();
		panel1.add(autoButton);
		panel1.add(manualButton);
		add(relayControl);
		panel1.add(relayControl);
		panel.add(panel1);
		horizontalPanel.add(panel);

		ClickHandler clickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
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
		manualButton.addClickHandler(clickHandler);

		initWidget(framePanel);
	}

	public void refreshSchedule(RegisterSchedule registerSchedule) {
		autoButton.setEnabled(true);
		manualButton.setEnabled(true);
		scheduleCanvas.refresh(registerSchedule.items);
		autoButton.setValue(registerSchedule.enabled);
		manualButton.setValue(!registerSchedule.enabled);
		relayControl.setEnabled(!registerSchedule.enabled);
	}

	public void enableSchedule(boolean value) {
		schedulePanel.clear();
		if (value)
			schedulePanel.add(scheduleCanvas);
	}
}
