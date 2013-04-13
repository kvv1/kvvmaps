package kvv.controllers.client;

import java.util.Map;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ObjectDescr;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controllers")
public interface ControllersService extends RemoteService {
	int getReg(int addr, int reg) throws Exception;

	void setReg(int addr, int reg, int val) throws Exception;
	
	Map<Integer, Integer> getRegs(int addr) throws Exception;

	ControllerDescr[] getControllers() throws Exception;

	ObjectDescr[] getObjects() throws Exception;
	
	String[] getCommands() throws Exception;

	void execCommand(String cmd) throws Exception;
	
	String upload(int addr, String name);
}
