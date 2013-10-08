package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.history.History;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("schedule")
public interface ScheduleService extends RemoteService {
	Schedule getSchedule() throws Exception;

	void enable(String regName, boolean b) throws Exception;

	History getHistory(Date date);

	void setSchedule(Schedule sched) throws Exception;

	void update(String regName, RegisterSchedule registerSchedule) throws Exception;

	String loadHistoryFile();
}
