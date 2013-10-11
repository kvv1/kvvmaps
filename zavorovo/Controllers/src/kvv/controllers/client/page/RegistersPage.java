package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.history.shared.History;
import kvv.controllers.history.shared.HistoryItem;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RegistersPage extends ControlComposite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	private final ArrayList<AutoRelayControl> objects = new ArrayList<AutoRelayControl>();
	private final VerticalPanel itemPanel = new VerticalPanel();

	private final RadioButton historyOff = new RadioButton("history", "Выкл");
	private final RadioButton historyToday = new RadioButton("history",
			"Сегодня");
	private final RadioButton historyYesterday = new RadioButton("history",
			"Вчера");

	public RegistersPage() {

		Button refreshButton = new Button("Обновить");
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		// final CheckBox enableSchedule = new
		// CheckBox("Показывать расписание");
		// enableSchedule.addClickHandler(new ClickHandler() {
		//
		// @Override
		// public void onClick(ClickEvent event) {
		// for (AutoRelayControl c : objects.values())
		// c.enableSchedule(enableSchedule.getValue());
		// }
		// });

		historyOff.setValue(true);

		ClickHandler historyClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		};

		historyOff.addClickHandler(historyClickHandler);
		historyToday.addClickHandler(historyClickHandler);
		historyYesterday.addClickHandler(historyClickHandler);

		HorizontalPanel historyRadioPanel = new HorizontalPanel();
		historyRadioPanel.add(historyOff);
		historyRadioPanel.add(historyToday);
		historyRadioPanel.add(historyYesterday);

		CaptionPanel historyPanel = new CaptionPanel("Показывать историю");
		historyPanel.add(historyRadioPanel);

		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(10);

		horizontalPanel.add(refreshButton);
		horizontalPanel.add(historyPanel);

		// Button bb = new Button("zzz");
		// bb.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// scheduleService.loadHistoryFile(new CallbackAdapter<String>() {
		// @Override
		// public void onSuccess(String result) {
		// TextArea txt = new TextArea();
		// txt.setText(result);
		// txt.setSize("600px", "600px");
		// horizontalPanel.add(txt);
		// }
		// });
		// }
		// });
		// horizontalPanel.add(bb);

		// horizontalPanel.add(enableSchedule);

		vertPanel.add(horizontalPanel);

		vertPanel.add(itemPanel);

		initWidget(vertPanel);

		final MouseMoveHandler mouseMoveHandler = new MouseMoveHandler() {

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				for (AutoRelayControl control : objects) {
					if (event == null)
						control.drawMarker(-1);
					else
						control.drawMarker(event.getX());
				}
			}
		};

		for (Register reg : ControllersPage.registers) {
			try {
				AutoRelayControl control = new AutoRelayControl(reg,
						mouseMoveHandler);
				itemPanel.add(control);
				add(control);
				objects.add(control);
			} catch (Exception e) {
			}
		}

		refresh();
	}

	public void refresh() {
		super.refresh();
		scheduleService.getSchedule(new CallbackAdapter<Schedule>() {
			@Override
			public void onSuccess(final Schedule schedule) {
				final Date dateForHistory = getDateForHistory(schedule.date);
				scheduleService.getHistory(dateForHistory,
						new CallbackAdapter<History>() {
							@Override
							public void onSuccess(History log) {
								for (AutoRelayControl c : objects) {
									RegisterSchedule registerSchedule = schedule.map
											.get(c.reg.name);
									ArrayList<HistoryItem> logItems = log == null ? null
											: log.items.get(c.reg.name);
									@SuppressWarnings("deprecation")
									int markerSeconds = schedule.date
											.getHours()
											* 3600
											+ schedule.date.getMinutes()
											* 60
											+ schedule.date.getSeconds();
									int historyEndSeconds = schedule.date
											.equals(dateForHistory) ? markerSeconds
											: 24 * 3600;
									c.refreshSchedule(registerSchedule,
											logItems, markerSeconds,
											historyEndSeconds);
								}
							}
						});
			}
		});
	}

	@SuppressWarnings("deprecation")
	private Date getDateForHistory(Date now) {
		if (historyToday.getValue())
			return now;
		if (historyYesterday.getValue()) {
			Date d = new Date(now.getYear(), now.getMonth(), now.getDate());
			return new Date(d.getTime() - 60000); // any time yesterday
		}
		return null;
	}

}
