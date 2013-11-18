package kvv.controllers.server;

import java.io.IOException;
import java.util.Date;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.history.HistoryFile;
import kvv.controllers.history.shared.History;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.ScheduleAndHistory;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends RemoteServiceServlet implements
		ScheduleService {

	@Override
	public synchronized void setSchedule(Schedule sched) throws Exception {
		try {
			Utils.jsonWrite(Constants.scheduleFile, sched);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public synchronized void update(String regName,
			RegisterSchedule registerSchedule) throws Exception {

		Schedule schedule = getSchedule();
		if (registerSchedule != null)
			schedule.map.put(regName, registerSchedule);
		else
			schedule.map.remove(regName);
		setSchedule(schedule);
	}

	@Override
	public synchronized void enable(String regName, boolean b) throws Exception {
		Schedule schedule = getSchedule();
		RegisterSchedule registerSchedule = schedule.map.get(regName);
		if (registerSchedule != null) {
			registerSchedule.enabled = b;
			setSchedule(schedule);
		}
	}

	@Override
	public String loadHistoryFile() {
		try {
			return Utils.readFile(HistoryFile.getLogFile(new Date())
					.getAbsolutePath());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public synchronized ScheduleAndHistory getScheduleAndHistory(Date date) {
		return new ScheduleAndHistory(getSchedule(), getHistory(date));
	}

	private synchronized Schedule getSchedule() {
		try {
			Schedule schedule = Utils.jsonRead(Constants.scheduleFile,
					Schedule.class);
			return schedule;
		} catch (Exception e) {
			return new Schedule();
		}
	}

	private synchronized History getHistory(Date date) {
		if (date == null)
			return null;
		History log = HistoryFile.load(date);
		return log;
	}

}
