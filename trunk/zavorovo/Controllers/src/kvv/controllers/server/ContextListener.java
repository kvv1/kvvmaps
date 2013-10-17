package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.controller.Controller;
import kvv.controllers.server.schedule.Scheduler;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		Controller.create();
		// HistoryLogger.create();

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
		Controller.close();
		// HistoryLogger.close();

		System.out.println("The Simple Web App. Has Been Removed");
		// this.context = null;
	}

}