package kvv.controllers.client;

import kvv.controllers.shared.Schedule;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void getSchedule(AsyncCallback<Schedule> callback);

	void setSchedule(String text, boolean on, AsyncCallback<Schedule> callback);

	void enable(String regName, boolean b, AsyncCallback<Void> callback);
}
