package kvv.controllers.server.controller;

import java.util.Date;
import java.util.HashMap;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Scheduler {
	private final IController wrapped;
	private final Controllers controllers;
	private volatile boolean stopped;

	public Scheduler(Controllers controllers, IController wrapped) {
		this.wrapped = wrapped;
		this.controllers = controllers;
		thread.start();
	}

	public void close() {
		stopped = true;
	}

	public HashMap<String, RegisterSchedule> map = new HashMap<String, RegisterSchedule>();

	private Thread thread = new Thread(Scheduler.class.getSimpleName()
			+ "Thread") {
		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			while (!stopped) {
				try {
					Thread.sleep(100);
					if (map.isEmpty()) {
						String sch = Utils.readFile(Constants.scheduleFile);
						map = Utils.fromJson(sch, Schedule.class).map;
					}

					if (!map.isEmpty()) {
						String regName = map.keySet().iterator().next();
						RegisterSchedule registerSchedule = map.remove(regName);
						Register reg = controllers.getRegister(regName);

						if (registerSchedule.enabled) {
							Date date = new Date();
							int minutes = date.getHours() * 60
									+ date.getMinutes();
							int value = registerSchedule.getValue(minutes);
							wrapped.setReg(reg.addr, reg.register, value);
						}
					}
				} catch (Exception e) {
				}
			}
		}
	};

}
