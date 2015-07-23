package kvv.controllers.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kvv.controller.register.AllRegs;
import kvv.controller.register.Rule;
import kvv.controllers.client.ControllersService;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.Statistics;
import kvv.stdutils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ControllersServiceImpl extends RemoteServiceServlet implements
		ControllersService {

	@Override
	public int getReg(int addr, int reg) throws Exception {
		return Context.getInstance().controller.getReg(addr, reg);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			Context.getInstance().controller.setReg(addr, reg, val);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	public static void print(AllRegs allRegs) {
		List<Integer> keys = new ArrayList<Integer>(allRegs.values.keySet());
		Collections.sort(keys);
		for (Integer reg : keys) {
			System.out.print(reg + ":" + allRegs.values.get(reg) + " ");
		}
		System.out.println();
	}

	@Override
	public AllRegs getRegs(int addr) throws Exception {
		try {
			AllRegs allRegs = Context.getInstance().controller.getAllRegs(addr);
			// print(allRegs);
			return allRegs;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public Integer hello(int addr) throws Exception {
		try {
			return Context.getInstance().controller.hello(addr);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public Rule[] getRules(int addr) throws Exception {
		try {
			return Context.getInstance().controller.getRules(addr);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void setRules(int addr, Rule[] rules) throws Exception {
		try {
			Context.getInstance().controller.setRules(addr, rules);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public Statistics getStatistics(boolean clear) {
		try {
			String s = Context.getInstance().controller.getStatistics(clear);
			return Utils.fromJson(s, Statistics.class);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
