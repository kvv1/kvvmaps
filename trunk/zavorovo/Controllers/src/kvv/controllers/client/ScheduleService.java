package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("schedule")
public interface ScheduleService extends RemoteService {
	void enable(String regName, boolean b) throws Exception;

	void update(String regName, RegisterSchedule registerSchedule)
			throws Exception;

	ScheduleAndHistory getScheduleAndHistory(Date date);
}
