package kvv.controllers.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.history.HistoryFile;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.controller.ExprCalculator;
import kvv.controllers.server.controller.Scheduler;
import kvv.controllers.shared.History;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.ScheduleAndHistory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends RemoteServiceServlet implements
		ScheduleService {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		synchronized (Context.looper) {
			super.service(arg0, arg1);
		}
	}

	@Override
	public synchronized RegisterSchedule update(String regName,
			RegisterSchedule registerSchedule) throws Exception {
		try {
			Scheduler scheduler = Context.getInstance().scheduler;
			scheduler.put(regName, registerSchedule);
			return registerSchedule;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public synchronized ScheduleAndHistory getScheduleAndHistory(Date date) {
		Scheduler scheduler = Context.getInstance().scheduler;
		Schedule schedule = scheduler.getSchedule();
		return new ScheduleAndHistory(schedule, getHistory(date));
	}

	private synchronized History getHistory(Date date) {
		if (date == null)
			return null;
		History log = HistoryFile.load(date);
		return log;
	}

	@Override
	public short eval(Integer addr, String expr) throws Exception {
		try {
			// ExprCalculator calculator = new ExprCalculator(addr, expr);
			// List<Byte> bytes = calculator.parse().getBytes();
			// return (short) calculator.eval(bytes);
			return (short) new ExprCalculator(addr, expr).parse().getValue();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

}
