package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void setSchedule(Schedule sched, AsyncCallback<Void> callback);

	void enable(String regName, boolean b, AsyncCallback<Void> callback);

	void update(String regName, RegisterSchedule registerSchedule,
			AsyncCallback<Void> callback);

	void loadHistoryFile(AsyncCallback<String> asyncCallback);

	void getScheduleAndHistory(Date date,
			AsyncCallback<ScheduleAndHistory> callback);
}
