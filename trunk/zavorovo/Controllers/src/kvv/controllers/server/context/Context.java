package kvv.controllers.server.context;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.controller.ControllerWrapperCached;
import kvv.controllers.server.controller.ControllerWrapperLogger;
import kvv.controllers.server.controller.ControllerWrapperUni;
import kvv.controllers.server.controller.Scheduler;
import kvv.controllers.server.unit.Units;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

public class Context {
	private static Context instance;
	private static boolean closedAll;

	public static synchronized Context getInstance() {
		if (instance == null && !closedAll)
			instance = new Context();
		return instance;
	}

	public static synchronized void stop() {
		closedAll = true;
		if (instance != null)
			instance.close();
		instance = null;
	}

	public static synchronized void reload() {
		if (instance != null)
			instance.close();
		instance = null;
	}

	public final Controllers controllers;
	public final Units units;
	public final IController controller;
	private final Scheduler scheduler;

	private void close() {
		units.close();
		scheduler.close();
		controller.close();
	}

	public Context() {
		controllers = new Controllers();

		String busURL = Utils.getProp(Constants.propsFile, "busURL");
		if (busURL == null)
			busURL = "http://localhost/rs485";
		controller = new ControllerWrapperCached(controllers,
				new ControllerWrapperLogger(controllers,
						new ControllerWrapperUni(controllers,
								new kvv.controllers.controller.Controller(
										busURL))));

		scheduler = new Scheduler(controllers, controller);
		units = new Units(controllers, controller);
	}
}
