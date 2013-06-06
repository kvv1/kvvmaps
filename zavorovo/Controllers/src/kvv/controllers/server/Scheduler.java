package kvv.controllers.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;

public class Scheduler extends Thread {

	static public class ScheduleLine {
		public Date date;
		public Register register;
		public int value;

		public ScheduleLine(Date date, Register register, int value) {
			this.date = date;
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
		int regIndex = 0;
		while (!stopped) {
			try {
				synchronized (this) {
					Schedule schedule = ScheduleFile.load();
					ArrayList<Register> regs = new ArrayList<Register>(
							schedule.map.keySet());
					Collections.sort(regs, new Comparator<Register>() {
						@Override
						public int compare(Register o1, Register o2) {
							return o1.name.compareTo(o2.name);
						}
					});

					if (regIndex >= regs.size())
						regIndex = 0;

					if (regs.size() > regIndex) {
						Register reg = regs.get(regIndex);
						RegisterSchedule registerSchedule = schedule.map
								.get(reg);
						if (registerSchedule.enabled) {
							Date date = new Date();
							int minutes = date.getHours() * 60
									+ date.getMinutes();
							int value = registerSchedule.getValue(minutes);
							exec(reg, value);
							sleep(1000);
						}
					}

					regIndex++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void exec(Register register, int value) throws Exception {
		ControllerDescr controllerDescr = Controllers.get(register.addr);
		if (controllerDescr.type == Type.MU110_8)
			value = value == 0 ? 0 : 1000;

		ControllersServiceImpl.controller.setReg(register.addr,
				register.register, value);
	}

}
