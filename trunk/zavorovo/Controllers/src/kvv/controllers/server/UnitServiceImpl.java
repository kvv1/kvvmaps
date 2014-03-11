package kvv.controllers.server;

import kvv.controllers.client.UnitService;
import kvv.controllers.server.context.Context;
import kvv.controllers.server.unit.Units;
import kvv.controllers.shared.ScriptData;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class UnitServiceImpl extends RemoteServiceServlet implements
		UnitService {
	@Override
	public void savePageScript(String pageName, String script) throws Exception {
		try {
			Context.getInstance().units.setScript(pageName, script);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void enableScript(String pageName, boolean b) throws Exception {
		try {
			Context.getInstance().units.enableScript(pageName, b);
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public ScriptData getScriptData(String pageName) throws Exception {
		try {
			Units units = Context.getInstance().units;
			return new ScriptData(units.getScript(pageName),
					units.scriptEnabled(pageName), units.getError(pageName));
		} catch (Throwable e) {
			throw new Exception(e.getMessage());
		}
	}
}
