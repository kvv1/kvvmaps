package kvv.controllers.client;

import kvv.controllers.register.AllRegs;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ObjectDescr;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void getReg(int addr, int reg, AsyncCallback<Integer> callback);

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

	void getControllers(AsyncCallback<ControllerDescr[]> callback);

	void getObjects(AsyncCallback<ObjectDescr[]> callback);

	void getRegs(int addr, AsyncCallback<AllRegs> callback);

	void upload(int addr, String name, AsyncCallback<String> callback);

	void getRegisters(AsyncCallback<Register[]> callback);

	void vmInit(int addr, AsyncCallback<Void> callback);

}
