package kvv.controllers.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void getSchedule(AsyncCallback<String> callback);
	void isOn(AsyncCallback<Boolean> callback);
	void setSchedule(String text, boolean on, AsyncCallback<Void> callback);
}
