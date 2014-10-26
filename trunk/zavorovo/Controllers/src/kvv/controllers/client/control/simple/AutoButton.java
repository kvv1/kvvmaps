package kvv.controllers.client.control.simple;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.page.ModePage;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.State;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;

public abstract class AutoButton extends Composite {
	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	private final CheckBox autoButton = new CheckBox("Расп.");

	private RegisterSchedule registerSchedule;

	public AutoButton() {
		autoButton.setValue(false);
		autoButton.setEnabled(false);
		autoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (registerSchedule == null)
					return;

				if (!ModePage.check()) {
					autoButton.setValue(!autoButton.getValue());
					return;
				}

				registerSchedule.state = autoButton.getValue() ? State.SCHEDULE
						: State.MANUAL;
				save(registerSchedule);
			}
		});

		initWidget(autoButton);
	}

	public void updateUI(RegisterSchedule registerSchedule) {
		this.registerSchedule = registerSchedule;
		if (registerSchedule != null && registerSchedule.items.size() != 0) {
			autoButton.setEnabled(true);
			autoButton.setValue(registerSchedule.state == State.SCHEDULE);
		} else {
			autoButton.setEnabled(false);
			autoButton.setValue(false);
		}
	}

	public abstract void save(RegisterSchedule registerSchedule);

}
