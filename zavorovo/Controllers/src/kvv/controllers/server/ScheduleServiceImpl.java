package kvv.controllers.server;

import java.util.Date;

import kvv.controllers.client.ScheduleService;
import kvv.controllers.history.HistoryFile;
import kvv.controllers.history.shared.History;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.controller.ExprCalculator;
import kvv.controllers.server.controller.Scheduler;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.ScheduleAndHistory;
import kvv.stdutils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ScheduleServiceImpl extends RemoteServiceServlet implements
		ScheduleService {

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

	/*
	 * @Override public synchronized RegisterSchedule enable(String regName,
	 * boolean b) throws Exception { try { Schedule schedule = getSchedule();
	 * RegisterSchedule registerSchedule = schedule.map.get(regName); if
	 * (registerSchedule != null) { registerSchedule.enabled = b;
	 * setSchedule(schedule); } return registerSchedule; } catch (Exception e) {
	 * throw new Exception(e.getMessage()); } }
	 * 
	 * @Override public synchronized RegisterSchedule enableExpr(String regName,
	 * boolean b) throws Exception { try { Schedule schedule = getSchedule();
	 * RegisterSchedule registerSchedule = schedule.map.get(regName); if
	 * (registerSchedule != null) { registerSchedule.exprEnabled = b;
	 * setSchedule(schedule); } return registerSchedule; } catch (Exception e) {
	 * throw new Exception(e.getMessage()); } }
	 */

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
	public short eval(String expr) throws Exception {
		try {
			return new ExprCalculator(Utils.utf2win(expr)).parse();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

}
