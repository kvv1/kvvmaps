package kvv.controllers.client;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {
	void saveControllersText(String text, AsyncCallback<Void> callbackAdapter);

	void loadControllersText(AsyncCallback<String> callbackAdapter);

	void loadPagesText(AsyncCallback<String> callbackAdapter);

	void savePagesText(String text, AsyncCallback<Void> callback);

	void getSystemDescr(AsyncCallback<SystemDescr> callback);

	void setSystemDescr(ControllerDescr[] controllerDescrs, UnitDescr[] unitDescrs, AsyncCallback<Void> callback);

	void loadControllerDefText(String type, AsyncCallback<String> callback);

	void saveControllerDefText(String type, String text,
			AsyncCallback<Void> callback);

	void loadControllerUIText(String type, AsyncCallback<String> callback);

	void saveControllerUIText(String type, String text,
			AsyncCallback<Void> callback);

	void delControllerType(String type, AsyncCallback<Void> callback);
}
