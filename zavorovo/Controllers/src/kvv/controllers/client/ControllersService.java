package kvv.controllers.client;

import java.util.HashMap;

import kvv.controller.register.Statistics;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controllers")
public interface ControllersService extends RemoteService {
	void setReg(int addr, int reg, int val) throws Exception;

	HashMap<Integer, Integer> getRegs(int addr) throws Exception;

	Integer hello(int addr) throws Exception;

	Statistics getStatistics(boolean clear);

	public String getModbusLog();

	void reset(int addr);

}
