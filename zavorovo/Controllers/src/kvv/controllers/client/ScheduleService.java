package kvv.controllers.client;

import java.util.Date;

import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("schedule")
public interface ScheduleService extends RemoteService {
	RegisterSchedule update(String regName, RegisterSchedule registerSchedule)
			throws Exception;
	ScheduleAndHistory getScheduleAndHistory(Date date);

	short eval(Integer addr, String expr) throws Exception;
	
/*
	RegisterSchedule enable(String regName, boolean b) throws Exception;

	RegisterSchedule enableExpr(String regName, boolean b) throws Exception;
*/	
}
