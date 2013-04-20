package kvv.controllers.client;

import kvv.controllers.register.SourceDescr;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SourcesServiceAsync {

	void delSourceFile(String name, AsyncCallback<Void> callback);

	void getSource(String name, AsyncCallback<String> callback);

	void setSource(String name, String text, AsyncCallback<Void> callback);

	void getSourceFiles(AsyncCallback<String[]> callback);

	void getSourceDescr(String controllerName,
			AsyncCallback<SourceDescr> callback);

}
