package kvv.controllers.server;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.controller.IController;
import kvv.controllers.server.utils.Constants;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ObjectDescr;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.Token;
import kvv.evlang.impl.ELReader;

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
	public String upload(int addr, String name) {
		EG1 parser = null;
		try {
			if (name == null) {
				controller.upload(addr, 0, new byte[] { 0, 0, 0 });
			} else {
				parser = new EG1(new ELReader(new FileReader(Constants.ROOT
						+ "/src/" + name)));

				parser.parse();
				byte[] bytes = parser.dump();
				controller.upload(addr, bytes);
			}
			return null;
		} catch (ParseException e) {
			Token t = parser.token;
			return " line " + t.beginLine + " : " + e.getMessage();
		} catch (IOException e) {
			return e.getMessage();
		}
	}
}
