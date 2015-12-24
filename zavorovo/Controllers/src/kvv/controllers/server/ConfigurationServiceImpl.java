package kvv.controllers.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kvv.controllers.client.ConfigurationService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.history.HistoryFile;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.stdutils.Utils;

@SuppressWarnings("serial")
public class ConfigurationServiceImpl extends LoginServlet implements
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
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(), "Сохранение конфигурации контроллеров и страниц (форма)");
			
			Utils.jsonWrite(Constants.controllersFile, controllerDescrs);
			Utils.jsonWrite(Constants.unitsFile, unitDescrs);
			Context.reload();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void saveControllersText(String text) throws Exception {
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(), "Сохранение конфигурации контроллеров (текст)");
			
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
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(), "Сохранение конфигурации страниц (текст)");
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
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(), "Сохранение типа " + type + " контроллеров");
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
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(), "Сохранение интерфейса контроллеров типа " + type);
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
		checkUser();
		try {
			HistoryFile.logUserAction(LoginServlet.getUserName(), "Удаление типа контроллеров " + type);
			new File(Constants.controllerTypesDir + type).delete();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
}
