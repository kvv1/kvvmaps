package kvv.controllers.client;

import kvv.controllers.register.AllRegs;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.Register;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controllers")
public interface ControllersService extends RemoteService {
	int getReg(int addr, int reg) throws Exception;

	void setReg(int addr, int reg, int val) throws Exception;

	AllRegs getRegs(int addr) throws Exception;

	ControllerDescr[] getControllers() throws Exception;

	String[] getObjects() throws Exception;

	String upload(int addr, String name);

	Register[] getRegisters() throws Exception;

	void vmInit(int addr) throws Exception;

	void save(String text) throws Exception;

	String load() throws Exception;

	String loadObjects() throws Exception;

	String loadRegisters() throws Exception;

	void saveRegisters(String text) throws Exception;

	void saveObjects(String text) throws Exception;
}
