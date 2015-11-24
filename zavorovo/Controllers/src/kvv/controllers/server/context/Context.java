package kvv.controllers.server.context;

import kvv.controllers.controller.Controller;
import kvv.controllers.controller.IController;
import kvv.controllers.controller.adu.ADUTransceiver;
import kvv.controllers.controller.adu.COMTransceiver;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.controller.ControllerWrapperCached;
import kvv.controllers.server.controller.ControllerWrapperGlobals;
import kvv.controllers.server.controller.ControllerWrapperLogger;
import kvv.controllers.server.controller.ControllerWrapperUni;
import kvv.controllers.server.controller.Scheduler;
import kvv.controllers.server.unit.Units;
import kvv.stdutils.Looper;
import kvv.stdutils.Utils;

public class Context {

	public static final Looper looper = new Looper();

	private static Context instance;
	private static boolean closedAll;

	public static synchronized Context getInstance() {
		if (instance == null && !closedAll)
			instance = new Context();
		return instance;
	}

	public static synchronized void start() {
		looper.start();
	}

	public static synchronized void stop() {
		looper.stop();
		synchronized (looper) {
			closedAll = true;
			if (instance != null)
				instance.close();
			instance = null;
		}
	}

	public static void reload() {
		if (instance != null)
			instance.close();
		instance = null;
		getInstance(); // to restart VMs
	}

	public final Controllers controllers;
	public final Units units;
	public final IController controller;
	public final Scheduler scheduler;
	//public final Ruler ruler; 

	private void close() {
		scheduler.close();
		//ruler.close();
		controller.close();
	}

	public Context() {
		controllers = new Controllers();

		// String busURL = Utils.getProp(Constants.propsFile, "busURL");
		// if (busURL == null)
		// busURL = "http://localhost/rs485";

		Controller c = new Controller();
		String com = Utils.getProp("c:/zavorovo/controller.properties", "COM");
		c.setModbusLine(new ADUTransceiver(new COMTransceiver(com, 400)));

		controller = new ControllerWrapperCached(controllers,
				new ControllerWrapperLogger(controllers,
						new ControllerWrapperGlobals(controllers,
								new ControllerWrapperUni(controllers, c))));

		units = new Units(controllers, controller);
		scheduler = new Scheduler(controllers, units, controller);
		//ruler = new Ruler(controllers, units, controller);
	}

}
