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
import kvv.controllers.shared.RegisterSchedule.Expr;
import kvv.controllers.shared.RegisterSchedule.State;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Scheduler {
	private final IController wrapped;
	private final Controllers controllers;
	private Schedule schedule;
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

		try {
			schedule = Utils.jsonRead(Constants.scheduleFile, Schedule.class);

			for (RegisterSchedule registerSchedule : schedule.map.values()) {
				if (registerSchedule.state == null)
					registerSchedule.state = State.MANUAL;
				if (registerSchedule.enabled)
					registerSchedule.state = State.SCHEDULE;
				registerSchedule.enabled = false;
			}
		} catch (Exception e) {
			schedule = new Schedule();
			e.printStackTrace();
		}

		thread.start();
	}

	public void close() {
		stopped = true;
	}

	private HashMap<String, RegisterSchedule> map = new HashMap<>();

	private Thread thread = new Thread(Scheduler.class.getSimpleName()
			+ "Thread") {
		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {

			while (!stopped) {
				try {
					Thread.sleep(100);
					synchronized (Scheduler.this) {
						if (map.isEmpty()) {
							map = new HashMap<>(schedule.map);
						}

						while (!map.isEmpty()) {
							String regName = map.keySet().iterator().next();
							RegisterSchedule registerSchedule = map
									.remove(regName);

							boolean ok = processReg(regName, registerSchedule);

							if (!ok)
								continue;

							break;
						}
					}
				} catch (Exception e) {
				}
			}
		}
	};

	public synchronized void put(String regName,
			RegisterSchedule registerSchedule) throws Exception {
		schedule.map.put(regName, registerSchedule);
		for (RegisterSchedule rs : schedule.map.values())
			for (Expr expr : rs.expressions)
				expr.errMsg = null;
		map.clear();
		Utils.jsonWrite(Constants.scheduleFile, schedule);
	}

	public synchronized Schedule getSchedule() {
		return Utils.fromJson(Utils.toJson(schedule), Schedule.class);
	}

	@SuppressWarnings("deprecation")
	private boolean processReg(String regName, RegisterSchedule registerSchedule)
			throws Exception {
		if (!regNames.contains(regName))
			return false;
		RegisterDescr reg = controllers.getRegister(regName);
		ControllerDescr controllerDescr = controllers.get(reg.controller);
		if (!controllerDescr.enabled)
			return false;

		switch (registerSchedule.state) {
		case SCHEDULE:
			Date date = new Date();
			int minutes = date.getHours() * 60 + date.getMinutes();
			int value = registerSchedule.getValue(minutes);
			wrapped.setReg(reg.addr, reg.register, value);
			return true;
		case EXPRESSION:
			if (registerSchedule.expressions == null
					|| registerSchedule.expressions.size() == 0)
				return false;

			for (Expr expr : registerSchedule.expressions)
				expr.errMsg = null;

			for (Expr expr : registerSchedule.expressions) {
				int value1 = 0;
				try {
					value1 = new ExprCalculator(Utils.utf2win(expr.expr))
							.parse();
				} catch (Exception e) {
					expr.errMsg = e.getMessage();
					continue;
				}
				wrapped.setReg(reg.addr, reg.register, value1);
				return true;
			}
			return true;
		default:
			return false;
		}
	}

}
