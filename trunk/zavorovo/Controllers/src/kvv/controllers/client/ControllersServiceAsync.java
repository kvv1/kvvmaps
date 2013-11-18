package kvv.controllers.client;

import java.util.Map;

import kvv.controllers.register.AllRegs;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.UnitDescr;
import kvv.controllers.shared.SystemDescr;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ControllersServiceAsync {

	void getReg(int addr, int reg, AsyncCallback<Integer> callback);

	void setReg(int addr, int reg, int val, AsyncCallback<Void> callback);

//	void getControllers(AsyncCallback<ControllerDescr[]> callback);

	void getRegs(int addr, AsyncCallback<AllRegs> callback);

	void upload(int addr, String name, AsyncCallback<String> callback);

	void vmInit(int addr, AsyncCallback<Void> callback);

	void saveControllersText(String text, AsyncCallback<Void> callbackAdapter);

	void loadControllersText(AsyncCallback<String> callbackAdapter);

//	void getPages(AsyncCallback<PageDescr[]> callback);

	void loadPagesText(AsyncCallback<String> callbackAdapter);

	void savePagesText(String text, AsyncCallback<Void> callback);

	void savePageScript(String pageName, String script,
			AsyncCallback<Void> callback);

	void enableScript(String pageName, boolean b, AsyncCallback<Void> callback);

	void getVMErrors(AsyncCallback<Map<String, String>> callback);

	void getSystemDescr(AsyncCallback<SystemDescr> callback);

}
