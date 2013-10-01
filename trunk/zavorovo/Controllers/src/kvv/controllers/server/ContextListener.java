package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.controller.Controller;
import kvv.controllers.server.utils.MyLogger;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.utils.Constants;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		try {
			String busURL = Utils.getProp(Constants.propsFile, "busURL");
			if (busURL == null)
				busURL = "http://localhost/rs485";
			ControllersServiceImpl.controller = new ControllerWrapper(
					new Controller(busURL));
		} catch (Exception e) {
			e.printStackTrace();
		}

		HistoryLogger.instance = new HistoryLogger();

		Scheduler.instance = new Scheduler();

		System.out.println("The Simple Web App. Is Ready");
	}

	@SuppressWarnings("deprecation")
	public void contextDestroyed(ServletContextEvent event) {
		if (Scheduler.instance != null) {
			Scheduler.instance.stopped = true;
			Scheduler.instance.stop();
			Scheduler.instance = null;
		}
		if (HistoryLogger.instance != null) {
			HistoryLogger.instance.stopped = true;
			HistoryLogger.instance.stop();
			HistoryLogger.instance = null;
		}

		MyLogger.stopLogger();

		System.out.println("The Simple Web App. Has Been Removed");
		// this.context = null;
	}

}