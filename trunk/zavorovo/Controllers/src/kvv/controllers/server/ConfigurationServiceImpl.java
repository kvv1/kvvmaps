package kvv.controllers.server;

import java.io.IOException;
import java.util.Date;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.SystemDescr;
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
			return systemDescr;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void setSystemDescr(SystemDescr sd) throws Exception {
		try {
			Controllers.save(sd.controllers);
			Units.save(sd.units);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void saveControllersText(String text) throws Exception {
		Utils.writeFile(Constants.controllersFile, text);
		Context.reload();
	}

	@Override
	public String loadControllersText() throws Exception {
		return Utils.readFile(Constants.controllersFile);
	}

	@Override
	public String loadPagesText() throws Exception {
		return Utils.readFile(Constants.unitsFile);
	}

	@Override
	public void savePagesText(String text) throws Exception {
		Utils.writeFile(Constants.unitsFile, text);
		Context.reload();
	}
}
