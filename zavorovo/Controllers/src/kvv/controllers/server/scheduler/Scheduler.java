package kvv.controllers.server.scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Constants;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.RegisterPresentation;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.RegisterSchedule.Expr;
import kvv.controllers.shared.RegisterSchedule.State;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.exprcalc.EXPR1;
import kvv.exprcalc.EXPR1_Base;
import kvv.exprcalc.EXPR1_Base.Rule;
import kvv.exprcalc.EXPR1_Base.RulePart;
import kvv.exprcalc.ParseException;
import kvv.exprcalc.TokenMgrError;
import kvv.stdutils.Utils;

public class Scheduler {
	private final IController controller;
	private final SystemDescr system;
	private Schedule schedule;
	private final Set<String> regNames = new HashSet<String>();

	public Scheduler(SystemDescr system, IController controller) {
		this.controller = controller;
		this.system = system;

		System.out.println("scheduler regs loading");

		try {
			for (UnitDescr ud : system.units)
				for (RegisterPresentation r : ud.registers)
					regNames.add(r.name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("scheduler regs loaded " + regNames.size());

		try {
			schedule = Utils.jsonRead(Constants.scheduleFile, Schedule.class);

			boolean removed = schedule.map.keySet().retainAll(regNames);

			for (RegisterSchedule registerSchedule : schedule.map.values())
				if (registerSchedule.state == null)
					registerSchedule.state = State.MANUAL;

			if (removed)
				Utils.jsonWrite(Constants.scheduleFile, schedule);

		} catch (Exception e) {
			schedule = new Schedule();
			e.printStackTrace();
		}

		Context.looper.post(r, 100);
		Context.looper.post(r1, 100);
	}

	private final Runnable r = new Runnable() {
		@Override
		public void run() {
			// System.out.print(".");
			try {
				if (map.isEmpty())
					map = new HashMap<>(schedule.map);

				while (!map.isEmpty()) {
					String regName = map.keySet().iterator().next();
					RegisterSchedule registerSchedule = map.remove(regName);

					boolean ok = processReg(regName, registerSchedule);

					if (ok)
						break;
				}
			} catch (Exception e) {
			}

			Context.looper.post(this, 100);
			// System.out.print(":");
		}
	};

	private final Runnable r1 = new Runnable() {
		@Override
		public void run() {
			// System.out.print(".");
			try {
				ControllerDescr[] cds = system.controllers;
				for (ControllerDescr cd : cds)
					reloadRules(cd);

			} catch (Exception e) {
			}

			Context.looper.post(this, 10000);
			// System.out.print(":");
		}
	};

	private void reloadRules(ControllerDescr cd) {
		ControllerType controllerType = system.controllerTypes.get(cd.type);
		if (controllerType == null || !controllerType.def.hasRules)
			return;

		List<Rule> rules = new ArrayList<>();

		l1: for (RegisterDescr rd : cd.registers) {
			RegisterSchedule registerSchedule = schedule.map.get(rd.name);
			if (registerSchedule != null
					&& registerSchedule.expressions != null
					&& registerSchedule.localExpr) {

				for (Expr expr : registerSchedule.expressions)
					expr.errMsg = null;

				List<RulePart> parts = new ArrayList<>();

				for (Expr expr : registerSchedule.expressions) {
					// System.out.println(expr.expr);
					try {
						parts.add(new RulePart(new Parser(cd.registers,
								expr.expr).parse().getBytes()));
					} catch (ParseException | IOException e) {
						expr.errMsg = e.getMessage();
						continue l1;
					}
				}

				rules.add(new Rule(rd.register, parts));
			}
		}

		List<Byte> rulePack = EXPR1_Base.packRules(rules.toArray(new Rule[0]));
		int[] vals = EXPR1_Base.toIntArr(EXPR1_Base.packToShortArr(rulePack));

		try {
			controller.setRegs(cd.addr, 258, vals);
		} catch (IOException e) {
		}
	}

	public void close() {
		Context.looper.remove(r);
		Context.looper.remove(r1);
	}

	private HashMap<String, RegisterSchedule> map = new HashMap<>();

	public void put(String regName, RegisterSchedule registerSchedule)
			throws Exception {
		schedule.map.put(regName, registerSchedule);
		for (RegisterSchedule rs : schedule.map.values())
			for (Expr expr : rs.expressions)
				expr.errMsg = null;
		map.clear();
		Utils.jsonWrite(Constants.scheduleFile, schedule);

		RegisterDescr register = system.getRegister(regName);
		if (register == null) {
			System.out.println("Scheduler.processReg reg " + regName
					+ " not found");
			return;
		}
		ControllerDescr cd = system.getController(register.controllerAddr);
		reloadRules(cd);
	}

	public Schedule getSchedule() {
		return Utils.fromJson(Utils.toJson(schedule), Schedule.class);
	}

	@SuppressWarnings("deprecation")
	private boolean processReg(String regName, RegisterSchedule registerSchedule)
			throws IOException {
		if (!regNames.contains(regName))
			return false;
		RegisterDescr reg = system.getRegister(regName);
		if (reg == null) {
			System.out.println("Scheduler.processReg reg " + regName
					+ " not found");
			return false;
		}

		ControllerDescr controllerDescr = system
				.getController(reg.controllerAddr);
		if (controllerDescr == null || !controllerDescr.enabled)
			return false;

		switch (registerSchedule.state) {
		case SCHEDULE:
			Date date = new Date();
			int minutes = date.getHours() * 60 + date.getMinutes();
			int value = registerSchedule.getValue(minutes);
			controller.setReg(reg.controllerAddr, reg.register, value);
			return true;
		case EXPRESSION:
			if (registerSchedule.localExpr)
				return false;

			if (registerSchedule.expressions == null
					|| registerSchedule.expressions.size() == 0)
				return false;

			for (Expr expr : registerSchedule.expressions)
				expr.errMsg = null;

			for (Expr expr : registerSchedule.expressions) {
				int value1 = 0;
				try {
					value1 = new ExprCalculator(null, expr.expr).parse()
							.getValue();
				} catch (Exception e) {
					expr.errMsg = e.getMessage();
					continue;
				}
				controller.setReg(reg.controllerAddr, reg.register, value1);
				return true;
			}
			return true;
		default:
			return false;
		}
	}

	class Parser extends EXPR1 {

		private final RegisterDescr[] registers;

		public Parser(RegisterDescr[] registers, String text) {
			super(text);
			this.registers = registers;
		}

		@Override
		public short getRegValue(String name) {
			throw new IllegalArgumentException("not supported");
		}

		@Override
		public short getRegNum(String name) throws ParseException {
			for (RegisterDescr rd : registers)
				if (rd.name.equals(name))
					return (short) rd.register;

			if (name.startsWith("R")) {
				try {
					return Short.parseShort(name.substring(1));
				} catch (NumberFormatException e) {
				}
			}

			throw new ParseException("Регистр " + name
					+ " не определен на данном контроллере");
		}

		@Override
		public String getRegName(int n) {
			throw new IllegalArgumentException("not supported");
		}

		@Override
		public short getRegValue(int n) {
			throw new IllegalArgumentException("not supported");
		}

		@Override
		public Expr parse() throws ParseException, IOException {
			try {
				return super.parse();
			} catch (TokenMgrError e) {
				throw new ParseException(e.getMessage());
			}
		}
	};
}
