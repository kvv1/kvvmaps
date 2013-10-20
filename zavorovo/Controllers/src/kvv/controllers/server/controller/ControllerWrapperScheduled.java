package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.Controllers;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

class ControllerWrapperScheduled extends Controller {

	public ControllerWrapperScheduled(IController wrapped) {
		super(wrapped);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		wrapped.setReg(addr, reg, val);
	}

	@Override
	public int getReg(int addr, int reg) throws IOException {
		return wrapped.getReg(addr, reg);
	}

	@Override
	public int[] getRegs(int addr, int reg, int n) throws IOException {
		return wrapped.getRegs(addr, reg, n);
	}

	@Override
	public AllRegs getAllRegs(int addr) throws IOException {
		return wrapped.getAllRegs(addr);
	}

	public HashMap<String, RegisterSchedule> map = new HashMap<String, RegisterSchedule>();

	@SuppressWarnings("deprecation")
	@Override
	public void step() {
		wrapped.step();

		try {

			if (map.isEmpty()) {
				String sch = Utils.readFile(Constants.scheduleFile);
				map = Utils.fromJson(sch, Schedule.class).map;
			}

			if (!map.isEmpty()) {
				String regName = map.keySet().iterator().next();
				RegisterSchedule registerSchedule = map.remove(regName);
				Register reg = Controllers.getInstance().getRegister(regName);

				if (registerSchedule.enabled) {
					Date date = new Date();
					int minutes = date.getHours() * 60 + date.getMinutes();
					int value = registerSchedule.getValue(minutes);
					Controller.getController().setReg(reg.addr, reg.register,
							value);
				}
			}
		} catch (Exception e) {
		}
	}

}
