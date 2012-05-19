package kvv.controllers.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("schedule")
public interface ScheduleService extends RemoteService{
	String getSchedule();
	boolean isOn();
	void setSchedule(String text, boolean on) throws Exception;
}
