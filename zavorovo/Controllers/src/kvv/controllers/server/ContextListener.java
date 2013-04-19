package kvv.controllers.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kvv.controllers.controller.Controller;
import kvv.controllers.router.RouterThread;
import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.MyLogger;
import kvv.controllers.server.utils.Props;

public class ContextListener implements ServletContextListener {

	// private ServletContext context = null;
	private static volatile RouterThread routerThread;

	public void contextInitialized(ServletContextEvent event) {
		try {
			boolean emul = Boolean.valueOf(Props.getProp(Constants.propsFile,
					"emul"));
			if (emul)
				ControllersServiceImpl.controller = new ControllerWrapper(
						new ControllerEmul());
			else {
				String controllerURL = Props.getProp(Constants.propsFile,
						"controllerURL");
				if (controllerURL == null)
					controllerURL = "http://localhost/controllers/controller";
				ControllersServiceImpl.controller = new ControllerWrapper(
						new Controller(controllerURL));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean logThread = Boolean.valueOf(Props.getProp(Constants.propsFile,
				"checkControllers"));
		if (logThread)
			LogThread.instance = new LogThread();

		boolean configRouter = Boolean.valueOf(Props.getProp(
				Constants.propsFile, "configRouter"));
		if (configRouter) {
			long routerCheckTime = 1000L * Integer.valueOf(Props.getProp(
					Constants.propsFile, "routerCheckTimeS"));
			String routerPassword = Props.getProp(Constants.propsFile,
					"routerPassword");
			String routerPublicIP = Props.getProp(Constants.propsFile,
					"routerPublicIP");
			String routerLocalIP = Props.getProp(Constants.propsFile,
					"routerLocalIP");
			String routerGatewayIP = Props.getProp(Constants.propsFile,
					"routerGatewayIP");
			routerThread = new RouterThread(routerCheckTime, routerPassword,
					routerPublicIP, routerLocalIP, routerGatewayIP);
		}

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
		if (routerThread != null) {
			routerThread.stopThread();
			routerThread = null;
		}

		Controllers.stopped = true;
		Controllers.thread.stop();

		MyLogger.stopLogger();

		System.out.println("The Simple Web App. Has Been Removed");
		// this.context = null;
	}

}