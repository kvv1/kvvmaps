package kvv.controllers.client;

import kvv.controllers.register.AllRegs;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void getReg(int addr, int reg, AsyncCallback<Integer> callback);

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

	void getRegs(int addr, AsyncCallback<AllRegs> callback);

	void upload(int addr, String name, AsyncCallback<String> callback);

	void vmInit(int addr, AsyncCallback<Void> callback);
}
