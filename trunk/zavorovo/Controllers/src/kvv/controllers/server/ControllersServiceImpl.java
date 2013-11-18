package kvv.controllers.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import kvv.controllers.client.ControllersService;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.utils.Constants;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.Token;
import kvv.evlang.impl.ExtRegisterDescr;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ControllersServiceImpl extends RemoteServiceServlet implements
		ControllersService {

	@Override
	public int getReg(int addr, int reg) throws Exception {
		return Context.getInstance().controller.getReg(addr, reg);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			Context.getInstance().controller.setReg(addr, reg, val);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public AllRegs getRegs(int addr) throws Exception {
		try {
			return Context.getInstance().controller.getAllRegs(addr);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String upload(int addr, String fileName) {
		EG1 parser = null;
		try {
			if (fileName == null) {
				Context.getInstance().controller.upload(addr, new byte[0]);
				storeSourceDescr(addr, null);
			} else {
				parser = new EG1(Constants.ROOT + "/src/" + fileName) {
					@Override
					protected ExtRegisterDescr getExtRegisterDescr(
							String extRegName) {
						return null;
					}
				};

				parser.parse();
				byte[] bytes = parser.dump();
				Context.getInstance().controller.upload(addr, bytes);

				storeSourceDescr(addr, fileName);
			}
			return null;
		} catch (ParseException e) {
			Token t = parser.token;
			return " line " + t.beginLine + " : " + e.getMessage();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public void vmInit(int addr) throws Exception {
		try {
			Context.getInstance().controller.vmInit(addr);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	private void storeSourceDescr(int addr, String gson) throws Exception {
		Properties props = new Properties();
		try {
			FileReader fr = new FileReader(Constants.srcFile);
			props.load(fr);
			fr.close();
		} catch (IOException e) {
		}
		try {
			ControllerDescr controllerDescr = Context.getInstance().controllers
					.get(addr);

			if (gson != null)
				props.setProperty(controllerDescr.name, gson);
			else
				props.remove(controllerDescr.name);

			FileWriter fw = new FileWriter(Constants.srcFile);
			props.store(fw, "");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

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

	@Override
	public void savePageScript(String pageName, String script) throws Exception {
		try {
			Context.getInstance().units.saveScript(pageName, script);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void enableScript(String pageName, boolean b) throws Exception {
		try {
			Context.getInstance().units.enableScript(pageName, b);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public Map<String, String> getVMErrors() {
		return Context.getInstance().units.getVMErrors();
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

	// @Override
	// public PageDescr[] getPages() throws Exception {
	// try {
	// return Pages.getPages();
	// } catch (FileNotFoundException e) {
	// return null;
	// } catch (IOException e) {
	// throw new Exception(e.getMessage());
	// }
	// }
	//
	// @Override
	// public ControllerDescr[] getControllers() throws Exception {
	// return Controllers.getInstance().getControllers();
	// }

	// return new Date().getTimezoneOffset();
}
