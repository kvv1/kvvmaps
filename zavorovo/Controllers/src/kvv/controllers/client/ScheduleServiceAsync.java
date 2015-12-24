package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void saveSchedule(String regName, RegisterSchedule registerSchedule,
			String comment, AsyncCallback<Void> callback);

	void getScheduleAndHistory(Date date,
			AsyncCallback<ScheduleAndHistory> callback);

	void eval(Integer addr, String expr, AsyncCallback<Short> callback);			
}
