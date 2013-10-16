package kvv.controllers.client.control;

import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.client.CallbackAdapter;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.page.AutoRelayControl;
import kvv.controllers.history.shared.History;
import kvv.controllers.history.shared.HistoryItem;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;

public abstract class ControlCompositeWithDiagrams extends ControlComposite {

	private final ScheduleServiceAsync scheduleService = GWT
			.create(ScheduleService.class);

	protected abstract Date getDateForHistory(Date now);

	public final ArrayList<AutoRelayControl> diagrams = new ArrayList<AutoRelayControl>();
	protected final MouseMoveHandler mouseMoveHandler = new MouseMoveHandler() {

		@Override
		public void onMouseMove(MouseMoveEvent event) {
			for (AutoRelayControl control : diagrams) {
				if (event == null)
					control.drawMarker(-1);
				else
					control.drawMarker(event.getX());
			}
		}
	};

	protected void refreshDiagrams() {
		scheduleService.getSchedule(new CallbackAdapter<Schedule>() {
			@Override
			public void onSuccess(final Schedule schedule) {
				final Date dateForHistory = getDateForHistory(schedule.date);
				scheduleService.getHistory(dateForHistory,
						new CallbackAdapter<History>() {
							@Override
							public void onSuccess(History log) {
								for (AutoRelayControl c : diagrams) {
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
}
