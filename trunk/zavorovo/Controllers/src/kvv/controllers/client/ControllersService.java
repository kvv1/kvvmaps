package kvv.controllers.client;

import kvv.controllers.register.AllRegs;
import kvv.controllers.register.Rule;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controllers")
public interface ControllersService extends RemoteService {
	int getReg(int addr, int reg) throws Exception;

	void setReg(int addr, int reg, int val) throws Exception;

	AllRegs getRegs(int addr) throws Exception;

	String upload(int addr, String name);

	void vmInit(int addr) throws Exception;

	Integer hello(int addr) throws Exception;

	Rule[] getRules(int addr) throws Exception;
}
