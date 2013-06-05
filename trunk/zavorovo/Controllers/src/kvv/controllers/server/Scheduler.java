package kvv.controllers.server;

import java.util.Date;
import java.util.List;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.shared.Register;

public class Scheduler extends Thread {

	static public class Command {
		public Register register;
		public int value;

		public Command(Register register, int value) {
			this.register = register;
			this.value = value;
		}
	}

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
						if (schedFile.enabled && schedFile.lines != null) {
							for (String line : schedFile.lines) {
								List<Command> commands = ScheduleFile
										.parseLine(line, date);
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
		ControllerDescr controllerDescr = Controllers.get(cmd.register.addr);
		if (controllerDescr.type == Type.MU110_8)
			cmd.value = cmd.value == 0 ? 0 : 1000;

		ControllersServiceImpl.controller.setReg(cmd.register.addr,
				cmd.register.register, cmd.value);
	}

}
