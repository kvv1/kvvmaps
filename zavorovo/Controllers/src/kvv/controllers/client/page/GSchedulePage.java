package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.register.AllRegs;
import kvv.controllers.shared.History;
import kvv.controllers.shared.ObjectDescr;
import kvv.controllers.shared.ObjectDescr.Type;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GSchedulePage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final MultiMap<Integer, AutoRelayControl> objects = new MultiMap<Integer, AutoRelayControl>();
	private final VerticalPanel itemPanel = new VerticalPanel();

	private final RadioButton historyOff = new RadioButton("history", "Выкл");
	private final RadioButton historyToday = new RadioButton("history",
			"Сегодня");
	private final RadioButton historyYesterday = new RadioButton("history",
			"Вчера");

	public GSchedulePage() {

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

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setSpacing(10);

		horizontalPanel.add(refreshButton);
		horizontalPanel.add(historyPanel);

		// horizontalPanel.add(enableSchedule);

		vertPanel.add(horizontalPanel);

		vertPanel.add(itemPanel);

		initWidget(vertPanel);

		final MouseMoveHandler mouseMoveHandler = new MouseMoveHandler() {

			@Override
			public void onMouseMove(MouseMoveEvent event) {
				for (AutoRelayControl control : objects.values()) {
					if (event == null)
						control.drawMarker(-1);
					else
						control.drawMarker(event.getX());
				}
			}
		};

		scheduleService.getSchedule(new CallbackAdapter<Schedule>() {
			@Override
			public void onSuccess(final Schedule schedule) {
				controllersService
						.getObjects(new CallbackAdapter<ObjectDescr[]>() {
							@Override
							public void onSuccess(
									final ObjectDescr[] objectDescrs) {
								controllersService
										.getRegisters(new CallbackAdapter<Register[]>() {
											@Override
											public void onSuccess(
													Register[] registers) {
												ArrayList<Register> regs = new ArrayList<Register>();
												for (ObjectDescr objectDescr : objectDescrs)
													if (objectDescr != null
															&& objectDescr.type == Type.RELAY) {
														Register reg = new Register(
																objectDescr.register,
																objectDescr.controller,
																objectDescr.addr,
																objectDescr.reg);
														regs.add(reg);
													}

												for (Register reg : schedule.map
														.keySet())
													if (!regs.contains(reg))
														regs.add(reg);

												for (Register reg : registers)
													if (!regs.contains(reg))
														regs.add(reg);

												for (Register reg : regs) {
													try {
														AutoRelayControl control = new AutoRelayControl(
																reg.addr, reg,
																mouseMoveHandler);
														itemPanel.add(control);
														objects.put(reg.addr,
																control);
													} catch (Exception e) {
													}
												}

												scheduleService
														.getLog(getDateForHistory(schedule.date),
																new CallbackAdapter<History>() {
																	@Override
																	public void onSuccess(
																			History log) {
																		refreshSchedule(
																				schedule,
																				log);
																	}

																});
												refreshData();
											}
										});
							}
						});
			}
		});
	}

	private void refreshSchedule(Schedule schedule, History log) {
		for (AutoRelayControl c : objects.values()) {
			RegisterSchedule registerSchedule = schedule.map.get(c.reg);
			c.refreshSchedule(registerSchedule,
					log == null ? null : log.items.get(c.reg), schedule.date);
		}
	}

	private void refreshData() {
		for (final Integer addr : objects.keySet()) {
			final Set<AutoRelayControl> set = objects.get(addr);
			for (ControlComposite c : set)
				c.refresh(null);

			controllersService.getRegs(addr, new AsyncCallback<AllRegs>() {
				@Override
				public void onSuccess(AllRegs result) {
					for (AutoRelayControl c : set) {
						c.refresh(result);
					}
				}

				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
	}

	private void refresh() {
		scheduleService.getSchedule(new CallbackAdapter<Schedule>() {
			@Override
			public void onSuccess(final Schedule schedule) {
				scheduleService.getLog(getDateForHistory(schedule.date),
						new CallbackAdapter<History>() {
							@Override
							public void onSuccess(History log) {
								refreshSchedule(schedule, log);
							}
						});
			}
		});
		refreshData();
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
