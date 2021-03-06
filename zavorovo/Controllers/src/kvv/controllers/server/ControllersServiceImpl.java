package kvv.controllers.server;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controller.register.Statistics;
import kvv.controllers.client.ControllersService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;
import kvv.controllers.shared.RegisterDescr;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.gwtutils.server.login.UserService;

@SuppressWarnings("serial")
public class ControllersServiceImpl extends LoginServlet implements
		ControllersService {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		synchronized (Context.looper) {
			super.service(arg0, arg1);
		}
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		checkUser();
		try {
			RegisterDescr rd = Context.getInstance().system.getRegister(addr,
					reg);
			String regName = rd == null ? ("addr " + addr + " reg " + reg)
					: rd.name;
			HistoryFile.logUserAction(LoginServlet.getUserName(), regName
					+ " := " + val);

			Context.getInstance().controller.setReg(addr, reg, val);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public HashMap<Integer, Integer> getRegs(int addr) throws Exception {
		HashMap<Integer, Integer> allRegs = Context.getInstance().controller
				.getCachedRegs(addr, false);
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

	@Override
	public String getModbusLog() {
		String res = "";
		for (String s : Context.getInstance().getModbusLog())
			res += s + "\n";
		return res;
	}

	@Override
	public void reset(int addr) {
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(),
					"Ресет контроллера addr=" + addr);
			Context.getInstance().controller.reset(addr);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected UserService getUserService() {
		return ContextListener.userService;
	}

}
