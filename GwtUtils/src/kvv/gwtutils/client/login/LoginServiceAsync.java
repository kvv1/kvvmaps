package kvv.gwtutils.client.login;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {

	void getSessionId(AsyncCallback<String> callback);

	void getUser(AsyncCallback<String> callback);

	void login(String name, String passwordEncoded,
			AsyncCallback<Boolean> callback);

	void logout(AsyncCallback<Void> callback);

}
