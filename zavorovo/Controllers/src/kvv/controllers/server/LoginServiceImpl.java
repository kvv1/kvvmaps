package kvv.controllers.server;

import kvv.gwtutils.server.login.UserService;

@SuppressWarnings("serial")
public class LoginServiceImpl extends kvv.gwtutils.server.login.LoginServiceImpl{

	@Override
	protected UserService getUserService() {
		return ContextListener.userService;
	}

}
