package kvv.controllers.server;

import java.io.IOException;

import kvv.controllers.client.LogService;
import kvv.stdutils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LogServiceImpl extends RemoteServiceServlet implements LogService {

	@Override
	public String getLog() {
		try {
			return Utils.readFile(Constants.logFile);
		} catch (IOException e) {
			return "!!! error reading log file !!!";
		}
	}

}
