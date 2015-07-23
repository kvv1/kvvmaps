package kvv.controllers.server.unit;

import java.io.IOException;

import kvv.controllers.controller.IController;
import kvv.controllers.server.Controllers;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.utils.Constants;
import kvv.stdutils.Utils;

public class Units {

	public UnitDescr[] units;

	public static void save(UnitDescr[] units) throws Exception {
		Utils.jsonWrite(Constants.unitsFile, units);
		Context.reload();
	}

	private final Controllers controllers;
	private final IController controller;

	public Units(Controllers controllers, IController controller) {
		try {
			units = Utils.jsonRead(Constants.unitsFile, UnitDescr[].class);
		} catch (IOException e) {
			this.units = new UnitDescr[0];
		}

		this.controllers = controllers;
		this.controller = controller;
	}

}
