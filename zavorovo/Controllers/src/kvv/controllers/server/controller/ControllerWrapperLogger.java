package kvv.controllers.server.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.server.history.HistoryFile;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.SystemDescr;

public class ControllerWrapperLogger extends ControllerAdapter {

	public ControllerWrapperLogger(SystemDescr system, IController controller) {
		super(system, controller);
		HistoryFile.logValue(new Date(), null, null);
	}

	@Override
	public void close() {
		super.close();
		HistoryFile.logValue(new Date(), null, null);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws IOException {
		try {
			wrapped.setReg(addr, reg, val);
			log(addr, reg, val);
		} catch (IOException e) {
			log(addr, reg, null);
			throw e;
		}
	}

	@Override
	public Integer getReg(int addr, int reg) throws IOException {
		try {
			Integer val = wrapped.getReg(addr, reg);
			log(addr, reg, val);
			return val;
		} catch (IOException e) {
			log(addr, reg, null);
			throw e;
		}
	}

	@Override
	public Integer[] getRegs(int addr, int reg, int n) throws IOException {
		try {
			Integer[] res = wrapped.getRegs(addr, reg, n);
			for (int i = 0; i < res.length; i++)
				log(addr, reg + i, res[i]);
			return res;
		} catch (IOException e) {
			log(addr, null);
			throw e;
		}
	}

	private void log(int addr, int reg, Integer val) {
		RegisterDescr register = system.getRegister(addr, reg);
		if (register == null)
			return;
		logValue(register.name, val);
	}

	private void log(int addr, Map<Integer, Integer> values) {
		if (values == null) {
			ControllerDescr cd = system.getController(addr);
			if(cd == null)
				return;
			RegisterDescr[] regs = cd.registers;
			for (RegisterDescr reg : regs)
				logValue(reg.name, null);
			return;
		}
		for (int reg : values.keySet())
			log(addr, reg, values.get(reg));
	}

	private Map<String, Integer> lastValues = new HashMap<String, Integer>();
	private Date lastFileDate;

	@SuppressWarnings("deprecation")
	private void logValue(String register, Integer value) {
		Date date = new Date();
		// Date fileDate = new Date(date.getTime());
		Date fileDate = new Date(date.getYear(), date.getMonth(),
				date.getDate());
		fileDate.setHours(0);
		fileDate.setMinutes(0);
		fileDate.setSeconds(0);

		// date = new Date(date.getYear(), date.getMonth(), date.getDate());

		if (!fileDate.equals(lastFileDate)) {
			for (String reg : lastValues.keySet())
				logValue(fileDate, reg, lastValues.get(reg));
			lastFileDate = fileDate;
		}

		Integer lastValue = lastValues.get(register);
		if (value == null && lastValue != null || value != null
				&& !value.equals(lastValue))
			logValue(date, register, value);

	}

	private void logValue(Date date, String register, Integer value) {
		HistoryFile.logValue(date, register, value);
		lastValues.put(register, value);
	}

}
