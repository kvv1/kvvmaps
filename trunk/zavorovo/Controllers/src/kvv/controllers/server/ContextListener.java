package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.controller.Controller;
import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.MyLogger;
import kvv.controllers.server.utils.Utils;

public class ContextListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		try {
			boolean emul = Boolean.valueOf(Utils.getProp(Constants.propsFile,
					"emul"));
			if (emul)
				ControllersServiceImpl.controller = new ControllerWrapper(
						new ControllerEmul());
			else {
				String busURL = Utils.getProp(Constants.propsFile,
						"busURL");
				if (busURL == null)
					busURL = "http://localhost/rs485";
				ControllersServiceImpl.controller = new ControllerWrapper(
						new Controller(busURL));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean logThread = Boolean.valueOf(Utils.getProp(Constants.propsFile,
				"checkControllers"));
		if (logThread)
			LogThread.instance = new LogThread();

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
		if (LogThread.instance != null) {
			LogThread.instance.stopped = true;
			LogThread.instance.stop();
			LogThread.instance = null;
		}

		Controllers.stopped = true;
		Controllers.thread.stop();

		MyLogger.stopLogger();

		System.out.println("The Simple Web App. Has Been Removed");
		// this.context = null;
	}

}