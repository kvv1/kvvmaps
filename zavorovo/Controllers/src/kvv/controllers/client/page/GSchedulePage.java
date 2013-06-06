package kvv.controllers.client.page;

import java.util.ArrayList;
import java.util.Set;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ControllersService;
import kvv.controllers.client.ControllersServiceAsync;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.ControlComposite;
import kvv.controllers.register.AllRegs;
import kvv.controllers.shared.ObjectDescr;
import kvv.controllers.shared.ObjectDescr.Type;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class GSchedulePage extends Composite {
	private VerticalPanel vertPanel = new VerticalPanel();
	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);
	private final ControllersServiceAsync controllersService = GWT
			.create(ControllersService.class);

	private final MultiMap<Integer, AutoRelayControl> objects = new MultiMap<Integer, AutoRelayControl>();
	VerticalPanel itemPanel = new VerticalPanel();

	public GSchedulePage() {

		Button refreshButton = new Button("Обновить");
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh();
			}
		});

		final CheckBox enableSchedule = new CheckBox("Показывать расписание");
		enableSchedule.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				for (AutoRelayControl c : objects.values())
					c.enableSchedule(enableSchedule.getValue());
			}
		});

		vertPanel.add(refreshButton);
		vertPanel.add(enableSchedule);

		vertPanel.add(itemPanel);

		initWidget(vertPanel);

		scheduleService.getSchedule(new CallbackAdapter<Schedule>() {
			@Override
			public void onSuccess(final Schedule schedule) {
				controllersService
						.getObjects(new AsyncCallback<ObjectDescr[]>() {
							@Override
							public void onSuccess(ObjectDescr[] objectDescrs) {
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
								for (Register reg : schedule.map.keySet())
									if (!regs.contains(reg))
										regs.add(reg);

								for (Register reg : regs) {
									try {
										AutoRelayControl control = new AutoRelayControl(
												reg.addr, reg);
										itemPanel.add(control);
										objects.put(reg.addr, control);
									} catch (Exception e) {
									}
								}
								refreshSchedule(schedule);
								refreshData();
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
			}
		});
	}

	private void refreshSchedule(Schedule schedule) {
		for (AutoRelayControl c : objects.values()) {
			RegisterSchedule registerSchedule = schedule.map.get(c.reg);
			if (registerSchedule != null)
				c.refreshSchedule(registerSchedule);
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
			public void onSuccess(Schedule result) {
				refreshSchedule(result);
			}
		});
		refreshData();
	}

}
