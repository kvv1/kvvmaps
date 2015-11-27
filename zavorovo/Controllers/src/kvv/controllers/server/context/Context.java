package kvv.controllers.server.context;

import kvv.controllers.controller.Controller;
import kvv.controllers.controller.ModbusLine;
import kvv.controllers.controller.adu.ADUTransceiver;
import kvv.controllers.controller.adu.COMTransceiver;
import kvv.controllers.server.Constants;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.controller.ControllerWrapperCached;
import kvv.controllers.server.controller.ControllerWrapperGlobals;
import kvv.controllers.server.controller.ControllerWrapperLogger;
import kvv.controllers.server.controller.Scheduler;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.ControllerDescr;
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

	public static void start() {
		looper.start();
	}

	public static void stop() {
		looper.stop();
		Context context = getInstance();
		closedAll = true;
		context.close();
	}

	public static void reload() {
		if (instance != null)
			instance.close();
		instance = null;
		getInstance(); // to restart VMs
	}

	public final Controllers controllers;
	public final Units units;
	public final ControllerWrapperCached controller;
	public final Scheduler scheduler;

	private void close() {
		scheduler.close();
		controller.close();
	}

	public Context() {
		controllers = new Controllers();

		String com = Utils.getProp(Constants.controllerPropsFile, "COM");
		ModbusLine modbusLine = new ADUTransceiver(new COMTransceiver(com));

		for (ControllerDescr cd : controllers.getControllers())
			modbusLine.setTimeout(cd.addr, cd.timeout);

		Controller c = new Controller();
		c.setModbusLine(modbusLine);

		controller = new ControllerWrapperCached(controllers,
				new ControllerWrapperLogger(controllers,
						new ControllerWrapperGlobals(controllers, c)));

		units = new Units(controllers, controller);
		scheduler = new Scheduler(controllers, units, controller);
	}

}
