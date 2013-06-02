package kvv.controllers.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sources")
public interface SourcesService extends RemoteService {
	String[] getSourceFiles();

	String getSource(String name) throws Exception;

	void setSource(String name, String text) throws Exception;

	void delSourceFile(String name);

	String getSourceFileName(String controllerName) throws Exception;

	String createSource(String text) throws Exception;
}
