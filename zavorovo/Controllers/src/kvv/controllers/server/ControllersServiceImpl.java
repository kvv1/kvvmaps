package kvv.controllers.server;

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
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ControllerDescr.Type;
import kvv.controllers.shared.ObjectDescr;
import kvv.controllers.utils.Constants;
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
		ControllerDescr controllerDescr = Controllers.getInstance().get(addr);
		if (controllerDescr.type == Type.MU110_8)
			val = val == 0 ? 0 : 1000;
		controller.setReg(addr, reg, val);
	}

	@Override
	public ControllerDescr[] getControllers() throws Exception {
		return Controllers.getInstance().getControllers();
	}

	@Override
	public kvv.controllers.shared.Register[] getRegisters() throws Exception {
		return Controllers.getInstance().getRegisters().values()
				.toArray(new kvv.controllers.shared.Register[0]);
	}

	@Override
	public ObjectDescr[] getObjects() throws Exception {
		ObjectDescr[] res = Utils.jsonRead(Constants.objectsFile,
				ObjectDescr[].class);
		for (ObjectDescr obj : res) {
			if (obj == null)
				continue;
			if (obj.controller != null)
				obj.addr = Controllers.getInstance().get(obj.controller).addr;
			if (obj.register != null) {
				kvv.controllers.shared.Register reg = Controllers.getInstance()
						.getRegister(obj.register);
				obj.reg = reg.register;
				obj.addr = Controllers.getInstance().get(reg.controller).addr;
			}
		}
		return res;
	}

	@Override
	public AllRegs getRegs(int addr) throws Exception {
		ControllerDescr controllerDescr = Controllers.getInstance().get(addr);
		if (controllerDescr.type == Type.MU110_8) {
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			int[] vals = controller.getRegs(addr, 0, 8);
			for (int i = 0; i < 8; i++)
				map.put(i, vals[i]);
			return new AllRegs(new ArrayList<RegisterUI>(), map);
		} else {
			AllRegs allRegs = controller.getAllRegs(addr);
			return allRegs;
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
	public void save(String text) throws Exception {
		try {
			Controllers.save(text);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String load() throws Exception {
		try {
			return Controllers.load();
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String loadObjects() throws Exception {
		try {
			return Utils.readFile(Constants.objectsFile);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

}
