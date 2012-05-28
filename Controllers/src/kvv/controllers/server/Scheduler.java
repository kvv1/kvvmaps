package kvv.controllers.server;

import java.util.Date;
import java.util.List;

import kvv.controllers.shared.Constants;
import kvv.controllers.shared.ControllerDescr;

public class Scheduler extends Thread {

	public Scheduler() {
		setDaemon(true);
		setPriority(MIN_PRIORITY);
		start();
	}

	public static volatile Scheduler instance;

	public boolean stopped;

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		long lastTime = 0;
		while (!stopped) {
			try {
				sleep(2000);
				synchronized (this) {
					Date date = new Date();
					date.setSeconds(0);
					date.setTime(date.getTime() - date.getTime() % 1000);
					long time = date.getTime();
					if (time != lastTime) {
						ScheduleFile schedFile = new ScheduleFile();
						schedFile.load();
						SetCommand[] defines = Utils.jsonRead(
								Constants.commandsFile, SetCommand[].class);
						if (schedFile.enabled && schedFile.lines != null) {
							for (String line : schedFile.lines) {
								List<SetCommand> commands = ScheduleFile
										.parseLine(line, date, defines);
								if (commands != null) {
									for (SetCommand cmd : commands) {
										exec(cmd);
										sleep(2000);
									}
								}
							}
						}
						lastTime = time;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void exec(SetCommand cmd) throws Exception {
		ControllerDescr d = Controllers.get(cmd.controller);
		if (d == null)
			return;

		ControllersServiceImpl.controller.setReg(d.addr, cmd.register,
				cmd.value);
	}

	public static void exec(String cmd) throws Exception {
		SetCommand[] defines = Utils.jsonRead(Constants.commandsFile,
				SetCommand[].class);

		for (SetCommand c : defines) {
			if (c != null && cmd.equals(c.name))
				exec(c);
		}
	}
}
