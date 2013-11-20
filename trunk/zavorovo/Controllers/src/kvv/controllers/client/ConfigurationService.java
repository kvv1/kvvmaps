package kvv.controllers.client;

import kvv.controllers.shared.SystemDescr;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("configuration")
public interface ConfigurationService  extends RemoteService {
	SystemDescr getSystemDescr() throws Exception;

	void saveControllersText(String text) throws Exception;

	String loadControllersText() throws Exception;

	String loadPagesText() throws Exception;

	void savePagesText(String text) throws Exception;


}
