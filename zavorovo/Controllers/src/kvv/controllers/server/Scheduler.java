package kvv.controllers.server;

import java.util.Date;
import java.util.List;

import kvv.controllers.server.utils.Utils;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.Command;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.utils.Constants;

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
						Command[] defines = Utils.jsonRead(
								Constants.commandsFile, Command[].class);
						if (schedFile.enabled && schedFile.lines != null) {
							for (String line : schedFile.lines) {
								List<Command> commands = ScheduleFile
										.parseLine(line, date, defines);
								if (commands != null) {
									for (Command cmd : commands) {
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

	private static void exec(Command cmd) throws Exception {
		Register register = Controllers.getRegister(cmd.register);

		ControllerDescr d = Controllers.get(register.controller);
		// if (d == null)
		// return;
		ControllerDescr controllerDescr = Controllers.get(d.addr);
		if (controllerDescr.type == Type.MU110_8)
			cmd.value = cmd.value == 0 ? 0 : 1000;

		ControllersServiceImpl.controller.setReg(d.addr, register.register,
				cmd.value);
	}

	public static void exec(String cmd) throws Exception {
		Command[] defines = Utils.jsonRead(Constants.commandsFile,
				Command[].class);

		try {
			for (Command c : defines) {
				if (c != null && cmd.equals(c.name))
					exec(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
