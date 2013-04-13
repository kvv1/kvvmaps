package kvv.controllers.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sources")
public interface SourcesService extends RemoteService{
	String[] getSourceFiles();
	String getSource(String name);
	void setSource(String name, String text);
	void delSourceFile(String name);
	String getSourceFile(String controllerName);
	void setSourceFile(String controllerName, String name);
}
