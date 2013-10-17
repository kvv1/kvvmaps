package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.history.shared.History;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void getSchedule(AsyncCallback<Schedule> callback);

	void setSchedule(Schedule sched, AsyncCallback<Void> callback);

	void enable(String regName, boolean b, AsyncCallback<Void> callback);

	void getHistory(Date date, AsyncCallback<History> callback);

	void update(String regName, RegisterSchedule registerSchedule, AsyncCallback<Void> callback);

	void loadHistoryFile(AsyncCallback<String> asyncCallback);
}