package kvv.controllers.client.page;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.shared.Schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SchedulePage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private TextArea text = new TextArea();
	private Button submit = new Button("Submit");
	//private CheckBox onoff = new CheckBox("Расписание включено");

	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	public SchedulePage() {
		text.setSize("400px", "600px");

		//vertPanel.add(onoff);
		vertPanel.add(text);
		vertPanel.add(submit);

//		onoff.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				if (!ModePage.controlMode) {
//					onoff.setValue(!((CheckBox) event.getSource()).getValue());
//					Window.alert("Режим управления не включен");
//					return;
//				}
//			}
//		});

		submit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ModePage.controlMode) {
					Window.alert("Режим управления не включен");
					return;
				}
				update();
			}
		});

		initWidget(vertPanel);
		refresh();
	}

	private void refresh() {
		scheduleService.getSchedule(new CallbackAdapter<Schedule>() {
			@Override
			public void onSuccess(Schedule result) {
				text.setText(result.text);
				//onoff.setValue(result.enabled);
			}
		});
	}

	private void update() {
		scheduleService.setSchedule(text.getText(), true/*onoff.getValue()*/,
				new CallbackAdapter<Schedule>() {
					@Override
					public void onSuccess(Schedule result) {
						text.setText(result.text);
						//onoff.setValue(result.enabled);
					}
				});
	}
}
