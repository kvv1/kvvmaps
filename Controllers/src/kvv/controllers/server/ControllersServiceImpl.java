package kvv.controllers.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kvv.controllers.client.ControllersService;
import kvv.controllers.controller.IController;
import kvv.controllers.shared.Constants;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ObjectDescr;

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
}
