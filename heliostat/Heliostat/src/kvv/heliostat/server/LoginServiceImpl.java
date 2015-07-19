package kvv.heliostat.server;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LoginServiceImpl extends kvv.gwtutils.server.login.LoginServiceImpl{

	private static Map<String, String> users = new HashMap<>();
	static {
		users.put("u1", "p1");
		users.put("u2", "p2");
	}

	@Override
	protected Map<String, String> getUsers() {
		return users;
	}
}
