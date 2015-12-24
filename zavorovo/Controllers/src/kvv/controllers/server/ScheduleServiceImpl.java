package kvv.controllers.server;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;
import kvv.controllers.server.scheduler.ExprCalculator;
import kvv.controllers.server.scheduler.Scheduler;
import kvv.controllers.shared.History;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.ScheduleAndHistory;
import kvv.gwtutils.server.login.LoginServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends LoginServlet implements
		ScheduleService {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		synchronized (Context.looper) {
			super.service(arg0, arg1);
		}
	}

	@Override
	public synchronized void saveSchedule(String regName,
			RegisterSchedule registerSchedule, String comment) throws Exception {
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(),
					"Сохранение расписания регистра " + regName + " ("
							+ comment + ")");
			Scheduler scheduler = Context.getInstance().scheduler;
			scheduler.put(regName, registerSchedule);
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
		History history = HistoryFile.load(date);
		return history;
	}

	@Override
	public short eval(Integer addr, String expr) throws Exception {
		try {
			return (short) new ExprCalculator(addr, expr).parse().getValue();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

}
