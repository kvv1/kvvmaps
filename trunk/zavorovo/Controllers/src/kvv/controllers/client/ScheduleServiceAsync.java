package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ScheduleServiceAsync {
	void update(String regName, RegisterSchedule registerSchedule,
			AsyncCallback<RegisterSchedule> callback);

	void getScheduleAndHistory(Date date,
			AsyncCallback<ScheduleAndHistory> callback);
/*
	void enable(String regName, boolean b, AsyncCallback<RegisterSchedule> callback);

	void enableExpr(String regName, boolean b,
			AsyncCallback<RegisterSchedule> callback);
*/

	void eval(String expr, AsyncCallback<Short> callback);			
}
