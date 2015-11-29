package kvv.controllers.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.stdutils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ConfigurationServiceImpl extends RemoteServiceServlet implements
		ConfigurationService {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		synchronized (Context.looper) {
			super.service(arg0, arg1);
		}
	}

	@Override
	public SystemDescr getSystemDescr() throws Exception {
		return Context.getInstance().system;
	}

	@Override
	public void setSystemDescr(ControllerDescr[] controllerDescrs,
			UnitDescr[] unitDescrs) throws Exception {
		try {
			Context.save(controllerDescrs, unitDescrs);
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
