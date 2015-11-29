package kvv.controllers.server;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controller.register.Statistics;
import kvv.controllers.client.ControllersService;
import kvv.controllers.server.context.Context;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ControllersServiceImpl extends RemoteServiceServlet implements
		ControllersService {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		synchronized (Context.looper) {
			super.service(arg0, arg1);
		}
	}

	@Override
	public int getReg(int addr, int reg) throws Exception {
		Integer res = Context.getInstance().controller.getReg(addr, reg);
		if (res == null)
			throw new Exception("Значение вне диапазона");
		return res;
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			Context.getInstance().controller.setReg(addr, reg, val);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public HashMap<Integer, Integer> getRegs(int addr) throws Exception {
		HashMap<Integer, Integer> allRegs = Context.getInstance().controller
				.getCachedRegs(addr);
		return allRegs;
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
	public Statistics getStatistics(boolean clear) {
		try {
			return Context.getInstance().controller.getStatistics(clear);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
