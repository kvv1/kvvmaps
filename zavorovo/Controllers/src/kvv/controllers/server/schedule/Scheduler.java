package kvv.controllers.server.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import kvv.controllers.server.Controllers;
import kvv.controllers.server.controller.Controller;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

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
		int regIndex = 0;
		while (!stopped) {
			synchronized (this) {
				try {
					String sched = null;
					Schedule schedule = null;
					ArrayList<String> regs = null;
					try {
						sched = Utils.readFile(Constants.scheduleFile);
						schedule = Utils.fromJson(sched, Schedule.class);
						regs = new ArrayList<String>(schedule.map.keySet());
						Collections.sort(regs);
					} catch (Exception e) {
						System.err.println("CANNOT READ SCHEDULE FILE: "
								+ sched);
						e.printStackTrace();
						throw e;
					}

					if (regIndex >= regs.size()) {
						regIndex = 0;
						sleep(1000);
					}
					if (regs.size() > regIndex) {
						Register reg = Controllers.getInstance().getRegister(
								regs.get(regIndex));
						RegisterSchedule registerSchedule = schedule.map
								.get(reg.name);
						if (registerSchedule.enabled) {
							Date date = new Date();
							int minutes = date.getHours() * 60
									+ date.getMinutes();
							int value = registerSchedule.getValue(minutes);
							Controller.getController().setReg(reg.addr,
									reg.register, value);
							sleep(1000);
						}
					}
				} catch (Exception e) {
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
					}
				}

				regIndex++;
			}
		}
	}
}
