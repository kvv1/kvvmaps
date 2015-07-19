package kvv.gwtutils.server.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import kvv.gwtutils.client.login.AuthException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServlet extends RemoteServiceServlet {
	public void checkUser() throws AuthException {
		HttpSession session = getSession();
		UserData ud = (UserData) session.getAttribute("UserData");
		if (ud == null)
			throw new AuthException("no permissions");
	}

	protected HttpSession getSession() {
		HttpServletRequest request = getThreadLocalRequest();
		HttpSession session = request.getSession();
		return session;
	}
}
