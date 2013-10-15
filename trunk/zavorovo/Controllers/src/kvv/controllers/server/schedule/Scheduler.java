package kvv.controllers.server.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import kvv.controllers.server.Controllers;
import kvv.controllers.server.ControllersServiceImpl;
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
				Schedule schedule;
				try {
					schedule = Utils.jsonRead(Constants.scheduleFile,
							Schedule.class);
				} catch (Exception e1) {
					try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
					break;
				}
				ArrayList<String> regs = new ArrayList<String>(
						schedule.map.keySet());
				Collections.sort(regs);

				try {
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
							exec(reg, value);
							sleep(1000);
						}
					}
				} catch (Exception e) {
				}

				regIndex++;
			}
		}
	}

	private static void exec(Register register, int value) throws Exception {
		// ControllerDescr controllerDescr = Controllers.getInstance().get(
		// register.addr);
		// if (controllerDescr.type == Type.MU110_8)
		// value = value == 0 ? 0 : 1000;

		ControllersServiceImpl.controller.setReg(register.addr,
				register.register, value);
	}

}
