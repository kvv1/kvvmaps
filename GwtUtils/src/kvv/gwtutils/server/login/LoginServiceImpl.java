package kvv.gwtutils.server.login;

import javax.servlet.http.HttpSession;

import kvv.gwtutils.client.login.AuthException;
import kvv.gwtutils.client.login.LoginService;
import kvv.simpleutils.src.MD5;

@SuppressWarnings("serial")
public abstract class LoginServiceImpl extends LoginServlet implements LoginService {

	@Override
	public String getSessionId() {
		HttpSession session = getSession();
		String id = session.getId();
		return id;
	}

	@Override
	public String getUser() {
		HttpSession session = getSession();
		String userName = (String) session.getAttribute("UserName");
		return userName;
	}

	@Override
	
	public boolean login(String name, String passwordEncoded) throws AuthException {
		UserData userData = getUserService().find(name);
		
		if (userData == null)
			throw new AuthException("AuthException");

		String s = MD5.calcMD5(userData.pwdHash + getSessionId());

		if (!s.equals(passwordEncoded))
			throw new AuthException("AuthException");

		HttpSession session = getSession();
		session.setAttribute("UserName", name);
		return true;
	}

	@Override
	public void logout() {
		HttpSession session = getSession();
		session.setAttribute("UserName", null);
	}

}
