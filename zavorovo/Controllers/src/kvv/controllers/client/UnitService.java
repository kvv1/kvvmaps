package kvv.controllers.client;

import kvv.controllers.shared.ScriptData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("unit")
public interface UnitService  extends RemoteService {
	ScriptData getScriptData(String pageName) throws Exception;

	void enableScript(String pageName, boolean b) throws Exception;

	void savePageScript(String pageName, String script) throws Exception;
}
