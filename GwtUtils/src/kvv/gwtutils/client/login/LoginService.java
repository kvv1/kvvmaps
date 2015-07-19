package kvv.gwtutils.client.login;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService  extends RemoteService {
	String getSessionId();

	String getUser();
	
	boolean login(String name, String passwordEncoded) throws AuthException;
	void logout();

}
