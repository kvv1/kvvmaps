package kvv.controllers.server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kvv.controllers.client.ControllersService;
import kvv.controllers.controller.IController;
import kvv.controllers.register.SourceDescr;
import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ObjectDescr;
import kvv.controllers.shared.SetCommand;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.Token;
import kvv.evlang.impl.ELReader;

import com.google.gson.Gson;
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
		controller.setReg(addr, reg, val);
	}

	@Override
	public ControllerDescr[] getControllers() throws Exception {
		return Utils.jsonRead(Constants.controllersFile,
				ControllerDescr[].class);
	}

	@Override
	public String[] getCommands() throws Exception {
		SetCommand[] defines = Utils.jsonRead(Constants.commandsFile,
				SetCommand[].class);
		List<String> res = new ArrayList<String>();

		for (SetCommand setCommand : defines) {
			if (setCommand != null && setCommand.name == null)
				res.add(null);
			else
				res.add(setCommand.name);
		}
		return res.toArray(new String[0]);
	}

	@Override
	public void execCommand(String cmd) throws Exception {
		Scheduler.exec(cmd);
	}

	@Override
	public ObjectDescr[] getObjects() throws Exception {
		ObjectDescr[] res = Utils.jsonRead(Constants.objectsFile,
				ObjectDescr[].class);
		for (ObjectDescr obj : res) {
			if (obj != null && obj.controller != null)
				obj.addr = Controllers.get(obj.controller).addr;
		}
		return res;

	}

	@Override
	public Map<Integer, Integer> getRegs(int addr) throws Exception {
		return controller.getRegs(addr);
	}

	@Override
	public String upload(int addr, String fileName) {
		EG1 parser = null;
		try {
			if (fileName == null) {
				controller.upload(addr, 0, new byte[] { 0, 0, 0 });
				storeSourceDescr(addr, null);
			} else {
				parser = new EG1(new ELReader(new FileReader(Constants.ROOT
						+ "/src/" + fileName)));

				parser.parse();
				byte[] bytes = parser.dump();
				controller.upload(addr, bytes);

				SourceDescr sourceDescr = new SourceDescr(fileName,
						parser.getRegisterDescription());

				String gson = new Gson().toJson(sourceDescr);
				System.out.println(gson);

				storeSourceDescr(addr, gson);
			}
			return null;
		} catch (ParseException e) {
			Token t = parser.token;
			return " line " + t.beginLine + " : " + e.getMessage();
		} catch (Exception e) {
			return e.getMessage();
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
			ControllerDescr controllerDescr = Controllers.get(addr);

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
}
