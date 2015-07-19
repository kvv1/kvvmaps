package kvv.gwtutils.server.login;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpSession;

import kvv.gwtutils.client.MD5;
import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.client.login.LoginService;

@SuppressWarnings("serial")
public class LoginServiceImpl extends LoginServlet implements LoginService {

	@Override
	public String getSessionId() {
		HttpSession session = getSession();
		String id = session.getId();
		return id;
	}

	protected Map<String, String> getUsers() {
		return Collections.emptyMap();
	}

	@Override
	public String getUser() {
		HttpSession session = getSession();
		UserData ud = (UserData) session.getAttribute("UserData");
		if (ud == null)
			return null;
		return ud.name;
	}

	@Override
	public boolean login(String name, String passwordEncoded)
			throws AuthException {
		String pw = getUsers().get(name);
		if (pw == null)
			throw new AuthException("AuthException");

		String pwdHash = MD5.calcMD5(name + pw);
		String s = MD5.calcMD5(pwdHash + getSessionId() + "salt");

		if (!s.equals(passwordEncoded))
			throw new AuthException("AuthException");

		HttpSession session = getSession();
		session.setAttribute("UserData", new UserData(name));
		return true;
	}

	@Override
	public void logout() {
		HttpSession session = getSession();
		session.setAttribute("UserData", null);
	}

}
