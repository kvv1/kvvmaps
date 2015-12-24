package kvv.gwtutils.server.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kvv.gwtutils.client.login.AuthException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoginServlet extends RemoteServiceServlet {
	public void checkUser() throws AuthException {
		// HttpSession session = getSession();
		// UserData ud = (UserData) session.getAttribute("UserData");

		UserData ud = user.get();
		if (ud == null)
			throw new AuthException("Not authenticated");
	}

	protected HttpSession getSession() {
		HttpServletRequest request = getThreadLocalRequest();
		HttpSession session = request.getSession();
		return session;
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		UserData ud = (UserData) session.getAttribute("UserData");
		user.set(ud);
		super.service(request, response);
	}

	public static ThreadLocal<UserData> user = new ThreadLocal<>();
	
	public static String getUserName() {
		UserData ud = user.get();
		if(ud == null)
			return null;
		return ud.name;
	}
}
