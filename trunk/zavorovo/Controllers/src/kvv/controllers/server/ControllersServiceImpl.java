package kvv.controllers.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import kvv.controllers.client.ControllersService;
import kvv.controllers.controller.IController;
import kvv.controllers.register.AllRegs;
import kvv.controllers.register.RegisterUI;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.shared.PageDescr;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.Token;
import kvv.evlang.impl.Context;
import kvv.evlang.impl.MyReader;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ControllersServiceImpl extends RemoteServiceServlet implements
		ControllersService {

	public static IController controller;

	@Override
	public int getReg(int addr, int reg) throws Exception {
		return controller.getReg(addr, reg);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			controller.setReg(addr, reg, val);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public ControllerDescr[] getControllers() throws Exception {
		return Controllers.getInstance().getControllers();
	}

	@Override
	public AllRegs getRegs(int addr) throws Exception {
		try {
			ControllerDescr controllerDescr = Controllers.getInstance().get(
					addr);
			if (controllerDescr.type == Type.MU110_8) {
				HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
				int[] vals = controller.getRegs(addr, 0, 8);
				for (int i = 0; i < 8; i++)
					map.put(i, vals[i]);
				return new AllRegs(addr, new ArrayList<RegisterUI>(), map);
			} else {
				AllRegs allRegs = controller.getAllRegs(addr);
				return allRegs;
			}
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String upload(int addr, String fileName) {
		EG1 parser = null;
		try {
			if (fileName == null) {
				controller.upload(addr, Context.dumpNull());
				storeSourceDescr(addr, null);
			} else {
				// parser = new EG1(new ELReader(new FileReader(Constants.ROOT
				// + "/src/" + fileName)));
				parser = new EG1(new MyReader(Constants.ROOT + "/src/"
						+ fileName));

				parser.parse();
				byte[] bytes = parser.dump();
				controller.upload(addr, bytes);

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
			controller.vmInit(addr);
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
			ControllerDescr controllerDescr = Controllers.getInstance().get(
					addr);

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
	public PageDescr[] getPages() throws Exception {
		try {
			PageDescr[] pages = Utils.jsonRead(Constants.pagesFile,
					PageDescr[].class);
			for (PageDescr page : pages) {
				try {
					page.script = Utils.readFile(Constants.ROOT + "/scripts/"
							+ page.name);
				} catch (Exception e) {
				}
			}
			return pages;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadPagesText() throws Exception {
		try {
			return Utils.readFile(Constants.pagesFile);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void savePagesText(String text) throws Exception {
		try {
			Utils.writeFile(Constants.pagesFile, text);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void savePageScript(String pageName, String script) throws Exception {
		new File(Constants.ROOT + "/scripts").mkdir();
		try {
			Utils.writeFile(Constants.ROOT + "/scripts/" + pageName, script);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

}
