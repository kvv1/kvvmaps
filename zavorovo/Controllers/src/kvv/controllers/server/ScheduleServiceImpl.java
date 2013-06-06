package kvv.controllers.server;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.Schedule;
import kvv.controllers.utils.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends RemoteServiceServlet implements
		ScheduleService {

	@Override
	public Schedule getSchedule() {
		return ScheduleFile.load();
	}

	@Override
	public Schedule setSchedule(String text, boolean on) throws Exception {
		Schedule sched = new Schedule(on);

		sched.enabled = on;
		sched.text = "";

		String[] lines = text.split("[\\r\\n]+", -1);
		for (int i = 0; i < lines.length; i++) {
			if (ScheduleFile.parseLine(lines[i], null) == null)
				lines[i] = "#ERR " + lines[i];
			sched.text += lines[i] + "\r\n";
		}

		sched.lines = lines;

		ScheduleFile.save(sched);
		return getSchedule();
	}

	@Override
	public void enable(String regName, boolean b) {
		Utils.changeProp(Constants.scheduleProps, regName, "" + b);
	}
}
