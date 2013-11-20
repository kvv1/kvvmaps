package kvv.controllers.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.SystemDescr;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ConfigurationServiceImpl extends RemoteServiceServlet implements
		ConfigurationService {
	@Override
	public void saveControllersText(String text) throws Exception {
		try {
			Controllers.save(text);
			Context.getInstance().units.loadScripts();
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadControllersText() throws Exception {
		try {
			return Controllers.load();
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadPagesText() throws Exception {
		try {
			return Units.load();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void savePagesText(String text) throws Exception {
		try {
			Units.save(text);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public SystemDescr getSystemDescr() throws Exception {
		try {
			SystemDescr systemDescr = new SystemDescr();
			systemDescr.controllerDescrs = Context.getInstance().controllers
					.getControllers();
			systemDescr.unitDescrs = Units.getUnits();
			systemDescr.timeZoneOffset = new Date().getTimezoneOffset();
			return systemDescr;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}
}
