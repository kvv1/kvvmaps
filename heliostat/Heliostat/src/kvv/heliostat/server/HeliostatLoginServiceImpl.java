package kvv.heliostat.server;

import kvv.gwtutils.server.login.LoginServiceImpl;
import kvv.gwtutils.server.login.UserService;

@SuppressWarnings("serial")
public class HeliostatLoginServiceImpl extends LoginServiceImpl{

	@Override
	protected UserService getUserService() {
		return ContextListener.userService;
	}

}
