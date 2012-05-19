package kvv.controllers.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LogServiceAsync {

	void getLog(AsyncCallback<String> callback);

}
