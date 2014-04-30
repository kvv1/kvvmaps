package kvv.controllers.server.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Scheduler {
	private final IController wrapped;
	private final Controllers controllers;
	private volatile boolean stopped;
	private final Set<String> regNames = new HashSet<String>();

	public Scheduler(Controllers controllers, Units units, IController wrapped) {
		this.wrapped = wrapped; 
		this.controllers = controllers;

		System.out.println("scheduler regs loading");

		try {
			if (units.units != null)
				for (UnitDescr ud : units.units)
					if (ud != null && ud.registers != null)
						for (RegisterPresentation r : ud.registers)
							if (r != null && r.name != null)
								regNames.add(r.name);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.out.println("scheduler regs loaded " + regNames.size());

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

					while (!map.isEmpty()) {
						String regName = map.keySet().iterator().next();
						RegisterSchedule registerSchedule = map.remove(regName);
						if (!regNames.contains(regName))
							continue;
						if (!registerSchedule.enabled)
							continue;
						RegisterDescr reg = controllers.getRegister(regName);
						ControllerDescr controllerDescr = controllers
								.get(reg.controller);
						if (!controllerDescr.enabled)
							continue;

						Date date = new Date();
						int minutes = date.getHours() * 60 + date.getMinutes();
						int value = registerSchedule.getValue(minutes);
						wrapped.setReg(reg.addr, reg.register, value);

						break;
					}
				} catch (Exception e) {
				}
			}
		}
	};

}
