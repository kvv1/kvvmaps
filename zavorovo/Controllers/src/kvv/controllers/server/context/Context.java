package kvv.controllers.server.context;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kvv.controllers.controller.Controller;
import kvv.controllers.controller.IController;
import kvv.controllers.controller.ModbusLine;
import kvv.controllers.controller.adu.ADUTransceiver;
import kvv.controllers.controller.adu.COMTransceiver;
import kvv.controllers.server.Constants;
import kvv.controllers.server.Logger;
import kvv.controllers.server.controller.ControllerWrapperAdj;
import kvv.controllers.server.controller.ControllerWrapperCached;
import kvv.controllers.server.controller.ControllerWrapperGlobals;
import kvv.controllers.server.scheduler.Scheduler;
import kvv.controllers.shared.ControllerDef;
import kvv.controllers.shared.ControllerDef.RegisterDef;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerType;
import kvv.controllers.shared.ControllerUI;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.stdutils.Looper;
import kvv.stdutils.Utils;

public class Context {

	public static final Looper looper = new Looper();

	private static Context instance;
	private static boolean closedAll;
	
	private ModbusLine modbusLine;

	public static synchronized Context getInstance() {
		if (instance == null && !closedAll)
			instance = new Context();
		return instance;
	}

	public static void start() {
		looper.start();
		looper.post(new Runnable() {
			@Override
			public void run() {
				Context.getInstance();
			}
		});
	}

	public static void stop() {
		looper.post(new Runnable() {
			@Override
			public void run() {
				looper.stop();
				Context context = getInstance();
				closedAll = true;
				context.close();
			}
		});
	}

	public static void reload() {
		if (instance != null)
			instance.close();
		instance = null;
		getInstance();
	}

	public final SystemDescr system = new SystemDescr();
	public IController controllerRaw;
	public ControllerWrapperCached controller;
	public final Scheduler scheduler;

	private void close() {
		scheduler.close();
		controller.close();
	}

	public Context() {
		loadUnits();
		loadControllers();
		loadTZ();
		createController();
		scheduler = new Scheduler(system, controller, controllerRaw);
	}

	private void createController() {
		String com = Utils.getProp(Constants.propsFile, "COM");
		modbusLine = new ADUTransceiver(new COMTransceiver(com));

		for (ControllerDescr cd : system.controllers)
			modbusLine.setTimeout(cd.addr, cd.timeout);

		controllerRaw = new Controller();
		controllerRaw.setModbusLine(modbusLine);

		controller = new ControllerWrapperCached(system,
						new ControllerWrapperGlobals(system,
								new ControllerWrapperAdj(system, controllerRaw)));
	}

	@SuppressWarnings("deprecation")
	private void loadTZ() {
		String tzo = Utils.getProp(Constants.propsFile, "timezoneOffset");
		if (tzo != null)
			system.timeZoneOffset = Integer.parseInt(tzo);
		else
			system.timeZoneOffset = new Date().getTimezoneOffset();
	}

	private void loadUnits() {
		try {
			system.units = Utils.jsonRead(Constants.unitsFile,
					UnitDescr[].class);
		} catch (IOException e) {
			system.units = new UnitDescr[0];
		}
	}

	private void loadControllers() {
		try {
			system.controllerTypes = new HashMap<>();

			FileFilter ff = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			};

			new File(Constants.controllerTypesDir).mkdirs();

			File[] dirs = new File(Constants.controllerTypesDir).listFiles(ff);
			for (File dir : dirs) {
				String name = dir.getName();
				try {
					ControllerType controllerType = new ControllerType();
					controllerType.def = Utils.jsonRead(new File(dir,
							"def.json").getAbsolutePath(), ControllerDef.class);
					controllerType.ui = Utils.jsonRead(new File(dir,
							"form.json").getAbsolutePath(), ControllerUI.class);
					resolveNames(controllerType.ui, controllerType.def);
					system.controllerTypes.put(name, controllerType);
				} catch (Exception e) {
					e.printStackTrace(Logger.out);
				}
			}

			system.controllers = Utils.jsonRead(Constants.controllersFile,
					ControllerDescr[].class);

			List<ControllerDescr> controllers = new ArrayList<ControllerDescr>();

			for (ControllerDescr cd : system.controllers) {
				if (cd.registers == null)
					cd.registers = new RegisterDescr[0];
				for (RegisterDescr reg : cd.registers) {
					reg.controller = cd.name;
					reg.controllerAddr = cd.addr;
				}
				controllers.add(cd);
			}

		} catch (Exception e) {
			e.printStackTrace(Logger.out);
		}
	}

	private void resolveNames(ControllerUI ui, ControllerDef def) {
		if (ui.regName != null)
			for (RegisterDef reg : def.registers)
				if (ui.regName.equals(reg.name))
					ui.reg = reg.n;

		if (ui.children != null)
			for (ControllerUI ui2 : ui.children)
				resolveNames(ui2, def);
	}

	public List<String> getModbusLog() {
		return modbusLine.getLog();
	}
}
