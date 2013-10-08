package kvv.controllers.server;

import java.io.IOException;
import java.util.Date;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.server.history.HistoryFile;
import kvv.controllers.server.history.HistoryLogger;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.history.History;
import kvv.controllers.utils.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends RemoteServiceServlet implements
		ScheduleService {

	@Override
	public synchronized Schedule getSchedule() throws Exception {
		try {
			Schedule schedule = Utils.jsonRead(Constants.scheduleFile,
					Schedule.class);
			schedule.date = new Date();
			return schedule;
		} catch (Exception e) {
			return new Schedule();
		}
	}

	@Override
	public synchronized void setSchedule(Schedule sched) throws Exception {
		try {
			sched.date = null;
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
	public History getHistory(Date date) {
		if (date == null)
			return null;
		History log = HistoryFile.load(date);
		return log;
	}

	@Override
	public String loadHistoryFile() {
		try {
			return Utils.readFile(HistoryLogger.getLogFile(new Date()).getAbsolutePath());
		} catch (IOException e) {
			return null;
		}
	}

}
