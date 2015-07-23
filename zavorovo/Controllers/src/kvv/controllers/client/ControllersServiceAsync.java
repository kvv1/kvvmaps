package kvv.controllers.client;

import kvv.controller.register.AllRegs;
import kvv.controller.register.Rule;
import kvv.controllers.shared.Statistics;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void getReg(int addr, int reg, AsyncCallback<Integer> callback);

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

	void getRegs(int addr, AsyncCallback<AllRegs> callback);

	void hello(int addr, AsyncCallback<Integer> callback);

	void getRules(int addr, AsyncCallback<Rule[]> callback);

	void setRules(int addr, Rule[] array, AsyncCallback<Void> callback);

	void getStatistics(boolean clear, AsyncCallback<Statistics> callback);
}
