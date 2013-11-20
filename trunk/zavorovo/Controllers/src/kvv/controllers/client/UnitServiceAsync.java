package kvv.controllers.client;

import kvv.controllers.shared.ScriptData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UnitServiceAsync {

	void enableScript(String pageName, boolean b, AsyncCallback<Void> callback);

	void getScriptData(String pageName, AsyncCallback<ScriptData> callback);

	void savePageScript(String pageName, String script,
			AsyncCallback<Void> callback);

}
