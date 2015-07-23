package kvv.controllers.client;

import kvv.controller.register.AllRegs;
import kvv.controller.register.Rule;
import kvv.controllers.shared.Statistics;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controllers")
public interface ControllersService extends RemoteService {
	int getReg(int addr, int reg) throws Exception;

	void setReg(int addr, int reg, int val) throws Exception;

	AllRegs getRegs(int addr) throws Exception;

	Integer hello(int addr) throws Exception;

	Rule[] getRules(int addr) throws Exception;

	void setRules(int addr, Rule[] array) throws Exception;
	
	Statistics getStatistics(boolean clear);
}
