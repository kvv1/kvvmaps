package kvv.gwtutils.server.login;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import kvv.gwtutils.client.login.AuthException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public abstract class LoginServlet extends RemoteServiceServlet {
	
	protected abstract UserService getUserService();
	
	public void checkUser() throws AuthException {
		UserData ud = getUserService().find(user.get());
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
		String userName = (String) session.getAttribute("UserName");
		user.set(userName);
		super.service(request, response);
	}

	private static ThreadLocal<String> user = new ThreadLocal<>();
	
	public static String getUserName() {
		return user.get();
	}
}
