package kvv.controllers.client.control;

import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.client.Controllers;
import kvv.controllers.client.ScheduleService;
import kvv.controllers.client.ScheduleServiceAsync;
import kvv.controllers.client.control.simple.AutoRelayControl;
import kvv.controllers.shared.HistoryItem;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleAndHistory;
import kvv.gwtutils.client.CallbackAdapter;

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

	@SuppressWarnings("deprecation")
	protected void refreshDiagrams() {
		final Date now = Server2Local(new Date());
		final Date dateForHistory = getDateForHistory(now);
		scheduleService.getScheduleAndHistory(Local2Server(dateForHistory),
				new CallbackAdapter<ScheduleAndHistory>() {
					@Override
					public void onSuccess(ScheduleAndHistory result) {
						for (AutoRelayControl c : diagrams) {
							ArrayList<HistoryItem> logItems = result.history == null ? null
									: result.history.items;
							int markerSeconds = now.getHours() * 3600
									+ now.getMinutes() * 60 + now.getSeconds();
							int historyEndSeconds = now.equals(dateForHistory) ? markerSeconds
									: 24 * 3600;
							RegisterSchedule registerSchedule = result.schedule.map
									.get(c.reg.name);
							if (registerSchedule == null)
								registerSchedule = new RegisterSchedule();
							c.refreshSchedule(registerSchedule, logItems,
									markerSeconds, historyEndSeconds);
						}
					}
				});
	}

	@SuppressWarnings("deprecation")
	private static Date Local2Server(Date d) {
		if (d == null)
			return d;
		return new Date(
				d.getTime()
						- (d.getTimezoneOffset() - Controllers.systemDescr.timeZoneOffset)
						* 60000);
	}

	@SuppressWarnings("deprecation")
	private static Date Server2Local(Date d) {
		if (d == null)
			return d;
		return new Date(
				d.getTime()
						+ (d.getTimezoneOffset() - Controllers.systemDescr.timeZoneOffset)
						* 60000);
	}
}
