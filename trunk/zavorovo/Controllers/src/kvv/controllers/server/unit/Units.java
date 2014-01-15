package kvv.controllers.server.unit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.RegisterDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.impl.ExtRegisterDescr;
import kvv.evlang.rt.RTContext;
import kvv.evlang.rt.VM;

public class Units {
	public static String load() throws IOException {
		return Utils.readFile(Constants.unitsFile);
	}

	public static void save(String text) throws IOException {
		Utils.writeFile(Constants.unitsFile, text);
		Context.reload();
	}

	public static UnitDescr[] getUnits() throws IOException {
		UnitDescr[] units = Utils.jsonRead(Constants.unitsFile,
				UnitDescr[].class);
		// for (UnitDescr unit : units) {
		// try {
		// unit.script = Utils.readFile(Constants.ROOT + "/scripts/"
		// + unit.name);
		// unit.scriptEnabled = scriptEnabled(unit.name);
		// } catch (Exception e) {
		// }
		// }
		return units;
	}

	public void setScript(String unitName, String script) throws Exception {
		try {
			new File(Constants.ROOT + "/scripts").mkdir();
			String fileName = Constants.ROOT + "/scripts/" + unitName;
			Utils.writeFile(fileName, script);
			loadScript(unitName);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	public String getScript(String unitName) throws Exception {
		try {
			return Utils.readFile(Constants.ROOT + "/scripts/" + unitName);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	public void enableScript(String unitName, boolean b) throws Exception {
		try {
			new File(Constants.ROOT + "/scripts").mkdir();
			Utils.changeProp(Constants.scriptsFile, unitName, "" + b);
			loadScript(unitName);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	public boolean scriptEnabled(String unitName) {
		return Boolean.valueOf(Utils.getProp(Constants.scriptsFile, unitName));
	}

	public RTContext parse(String unitName) throws FileNotFoundException,
			ParseException {

		EG1 parser = new EG1(Constants.ROOT + "/scripts/" + unitName) {
			@Override
			protected ExtRegisterDescr getExtRegisterDescr(String extRegName) {
				try {
					RegisterDescr reg = controllers.getRegister(extRegName);
					return new ExtRegisterDescr(reg.addr, reg.register);
				} catch (Exception e) {
					return null;
				}
			}
		};

		parser.parse();
		return parser.getRTContext();
	}

	private Thread thread = new Thread(Units.class.getSimpleName() + "Thread") {

		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		@Override
		public void run() {
			while (!stopped) {
				try {
					Thread.sleep(100);
					for (VM vm : vms.values())
						vm.step();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private final Controllers controllers;
	private Map<String, VM> vms = new HashMap<String, VM>();
	private Map<String, String> vmErrors = new HashMap<String, String>();
	private final IController controller;
	private volatile boolean stopped;

	public Units(Controllers controllers, IController controller) {
		this.controllers = controllers;
		this.controller = controller;
		try {
			loadScripts();
		} catch (IOException e) {
			e.printStackTrace();
		}
		thread.start();
	}

	public String getError(String pageName) {
		return vmErrors.get(pageName);
	}

	public synchronized void loadScripts() throws IOException {
		vms.clear();
		vmErrors.clear();

		UnitDescr[] units = Units.getUnits();
		for (UnitDescr unit : units)
			if (scriptEnabled(unit.name))
				_loadScript(unit.name);
	}

	public synchronized void loadScript(String unitName) throws IOException {
		vms.remove(unitName);
		vmErrors.remove(unitName);

		if (scriptEnabled(unitName))
			_loadScript(unitName);
	}

	private void _loadScript(String unitName) {
		try {
			VM vm = new VM1(parse(unitName), controller);
			vms.put(unitName, vm);
		} catch (Exception e) {
			vmErrors.put(unitName, e.getMessage());
			try {
				enableScript(unitName, false);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public void close() {
		stopped = true;
	}

}
