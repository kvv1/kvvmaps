package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.History;
import kvv.controllers.shared.Schedule;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("schedule")
public interface ScheduleService extends RemoteService {
	Schedule getSchedule();

	Schedule setSchedule(String text, boolean on) throws Exception;

	void enable(String regName, boolean b);

	History getLog(Date date);
}
