package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.server.rs485.Controller;
import kvv.controllers.server.rs485.ControllerEmul;
import kvv.controllers.server.rs485.Rs485;
import kvv.controllers.shared.Constants;

public class ContextListener implements ServletContextListener {

	// private ServletContext context = null;

	public void contextInitialized(ServletContextEvent event) {
		try {
			boolean emul = Boolean.valueOf(Utils.getProp(Constants.propsFile,
					"emul"));
			if (emul)
				ControllersServiceImpl.controller = new ControllerWrapper(
						new ControllerEmul());
			else
				ControllersServiceImpl.controller = new ControllerWrapper(
						new Controller(new Rs485(Utils.getProp(
								Constants.propsFile, "COM"))));
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean logThread = Boolean.valueOf(Utils.getProp(Constants.propsFile,
				"checkControllers"));
		if (logThread)
			LogThread.instance = new LogThread();

		boolean configRouter = Boolean.valueOf(Utils.getProp(
				Constants.propsFile, "configRouter"));
		if (configRouter)
			RouterThread.instance = new RouterThread();

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
		if (RouterThread.instance != null) {
			RouterThread.instance.stopped = true;
			RouterThread.instance.stop();
			RouterThread.instance = null;
		}

		Controllers.stopped = true;
		Controllers.thread.stop();

		ControllersServiceImpl.controller.close();
		
		Utils.stopLogger();

		System.out.println("The Simple Web App. Has Been Removed");
		// this.context = null;
	}

}