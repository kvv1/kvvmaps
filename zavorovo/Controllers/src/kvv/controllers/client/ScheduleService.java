package kvv.controllers.client;

import kvv.controllers.shared.Log;
import kvv.controllers.shared.Schedule;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("schedule")
public interface ScheduleService extends RemoteService {
	Schedule getSchedule();

	Schedule setSchedule(String text, boolean on) throws Exception;

	void enable(String regName, boolean b);

	Log getLog();
}
