package kvv.controllers.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FileServiceAsync {

	void get(String path, AsyncCallback<String> callback);

	void set(String path, String text, AsyncCallback<Void> callback);

}
