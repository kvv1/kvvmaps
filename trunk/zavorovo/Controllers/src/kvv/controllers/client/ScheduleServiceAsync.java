package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void enable(String regName, boolean b, AsyncCallback<Void> callback);

	void update(String regName, RegisterSchedule registerSchedule,
			AsyncCallback<Void> callback);

	void getScheduleAndHistory(Date date,
			AsyncCallback<ScheduleAndHistory> callback);
}
