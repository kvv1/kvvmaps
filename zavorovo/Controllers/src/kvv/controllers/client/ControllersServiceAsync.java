package kvv.controllers.client;

import kvv.controllers.register.AllRegs;
import kvv.controllers.register.Rule;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void getReg(int addr, int reg, AsyncCallback<Integer> callback);

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

	void getRegs(int addr, AsyncCallback<AllRegs> callback);

	void upload(int addr, String name, AsyncCallback<String> callback);

	void vmInit(int addr, AsyncCallback<Void> callback);

	void hello(int addr, AsyncCallback<Integer> callback);

	void getRules(int addr, AsyncCallback<Rule[]> callback);
}
