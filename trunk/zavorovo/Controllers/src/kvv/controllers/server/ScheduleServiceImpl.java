package kvv.controllers.server;

import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.SetCommand;
import kvv.controllers.client.ScheduleService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends RemoteServiceServlet implements
		ScheduleService {

	@Override
	public String getSchedule() {
		ScheduleFile sched = new ScheduleFile();
		sched.load();
		String text = "";
		if (sched.lines != null) {
			for (String line : sched.lines) {
				text = text + line + "\r\n";
			}
		}
		return text;
	}

	@Override
	public boolean isOn() {
		ScheduleFile sched = new ScheduleFile();
		sched.load();
		return sched.enabled;
	}

	@Override
	public void setSchedule(String text, boolean on) throws Exception {
		ScheduleFile sched = new ScheduleFile();

		SetCommand[] defines = Utils.jsonRead(
				Constants.commandsFile, SetCommand[].class);

		sched.enabled = on;

		String[] lines = text.split("[\\r\\n]+", -1);
		sched.lines = lines;
		for (int i = 0; i < lines.length; i++) {
			if (ScheduleFile.parseLine(lines[i], null, defines) == null)
				lines[i] = "#ERR " + lines[i];
		}

		sched.save();
	}
}
