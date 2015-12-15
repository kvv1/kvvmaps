package kvv.controllers.client;

import java.util.HashMap;

import kvv.controller.register.Statistics;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

	void getRegs(int addr, AsyncCallback<HashMap<Integer, Integer>> callback);

	void hello(int addr, AsyncCallback<Integer> callback);

	void getStatistics(boolean clear, AsyncCallback<Statistics> callback);

	void getModbusLog(AsyncCallback<String> callback);
}
