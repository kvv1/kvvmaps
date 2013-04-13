package kvv.controllers.client;

import java.util.Map;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.ObjectDescr;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void getReg(int addr, int reg, AsyncCallback<Integer> callback);

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

	void getControllers(AsyncCallback<ControllerDescr[]> callback);

	void getCommands(AsyncCallback<String[]> callback);

	void execCommand(String cmd, AsyncCallback<Void> callback);

	void getObjects(AsyncCallback<ObjectDescr[]> callback);

	void getRegs(int addr, AsyncCallback<Map<Integer, Integer>> callback);

	void upload(int addr, String name, AsyncCallback<String> callback);

}
