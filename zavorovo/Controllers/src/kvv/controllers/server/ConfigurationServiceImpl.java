package kvv.controllers.server;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ConfigurationServiceImpl extends RemoteServiceServlet implements
		ConfigurationService {
	@SuppressWarnings("deprecation")
	@Override
	public SystemDescr getSystemDescr() throws Exception {
		try {
			SystemDescr systemDescr = new SystemDescr();
			systemDescr.controllers = Context.getInstance().controllers
					.getControllers();
			systemDescr.units = Units.getUnits();
			systemDescr.timeZoneOffset = new Date().getTimezoneOffset();

			systemDescr.controllerTypes = Context.getInstance().controllers
					.getControllerTypes();
			return systemDescr;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void setSystemDescr(ControllerDescr[] controllerDescrs,
			UnitDescr[] unitDescrs) throws Exception {
		try {
			Controllers.save(controllerDescrs);
			Units.save(unitDescrs);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void saveControllersText(String text) throws Exception {
		try {
			Utils.writeFile(Constants.controllersFile, text);
			Context.reload();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadControllersText() throws Exception {
		try {
			return Utils.readFile(Constants.controllersFile);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadPagesText() throws Exception {
		try {
			return Utils.readFile(Constants.unitsFile);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void savePagesText(String text) throws Exception {
		try {
			Utils.writeFile(Constants.unitsFile, text);
			Context.reload();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadControllerDefText(String type) throws Exception {
		try {
			return Utils.readFile(Constants.controllerTypesDir + type
					+ "/def.json");
		} catch (Exception e) {
			return "{\r\n}\r\n";
		}
	}

	@Override
	public void saveControllerDefText(String type, String text)
			throws Exception {
		try {
			new File(Constants.controllerTypesDir + type).mkdirs();
			Utils.writeFile(Constants.controllerTypesDir + type + "/def.json",
					text);
			Context.reload();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadControllerUIText(String type) throws Exception {
		try {
			return Utils.readFile(Constants.controllerTypesDir + type
					+ "/form.json");
		} catch (Exception e) {
			return "{\r\n}\r\n";
		}
	}

	@Override
	public void saveControllerUIText(String type, String text) throws Exception {
		try {
			new File(Constants.controllerTypesDir + type).mkdirs();
			Utils.writeFile(Constants.controllerTypesDir + type + "/form.json",
					text);
			Context.reload();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void delControllerType(String type) throws Exception {
		try {
			new File(Constants.controllerTypesDir + type).delete();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
}
