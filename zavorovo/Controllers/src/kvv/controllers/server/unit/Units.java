package kvv.controllers.server.unit;

import java.io.File;
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

	public UnitDescr[] units;

	public static void save(UnitDescr[] units) throws Exception {
		Utils.jsonWrite(Constants.unitsFile, units);
		Context.reload();
	}

	public void setScript(String unitName, String script) throws Exception {
		new File(Constants.scriptsDir).mkdir();
		String fileName = Constants.scriptsDir + unitName;
		Utils.writeFile(fileName, script);
		enableScript(unitName, false);
	}

	public String getScript(String unitName) throws Exception {
		return Utils.readFile(Constants.scriptsDir + unitName);
	}

	public void enableScript(String unitName, boolean b) throws Exception {
		new File(Constants.scriptsDir).mkdir();
		Utils.changeProp(Constants.scriptsFile, unitName, "" + b);
		if (b)
			loadScript(unitName);
		else
			vms.remove(unitName);

	}

	public boolean scriptEnabled(String unitName) {
		return Boolean.valueOf(Utils.getProp(Constants.scriptsFile, unitName));
	}

	public RTContext parse(String unitName) throws IOException, ParseException {

		EG1 parser = new EG1(Constants.scriptsDir + unitName) {
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

		parser.parse(null);
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
		try {
			units = Utils.jsonRead(Constants.unitsFile, UnitDescr[].class);
		} catch (IOException e) {
			this.units = new UnitDescr[0];
		}

		this.controllers = controllers;
		this.controller = controller;
		loadScripts();
		thread.start();
	}

	public String getError(String pageName) {
		return vmErrors.get(pageName);
	}

	private synchronized void loadScripts() {
		vms.clear();
		vmErrors.clear();

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
