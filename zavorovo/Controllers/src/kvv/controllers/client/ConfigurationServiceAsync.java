package kvv.controllers.client;

import kvv.controllers.shared.SystemDescr;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {
	void saveControllersText(String text, AsyncCallback<Void> callbackAdapter);

	void loadControllersText(AsyncCallback<String> callbackAdapter);

	void loadPagesText(AsyncCallback<String> callbackAdapter);

	void savePagesText(String text, AsyncCallback<Void> callback);

	void getSystemDescr(AsyncCallback<SystemDescr> callback);
}
