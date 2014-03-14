package kvv.controllers.client;

import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.SystemDescr;
import kvv.controllers.shared.UnitDescr;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("configuration")
public interface ConfigurationService extends RemoteService {
	SystemDescr getSystemDescr() throws Exception;

	void saveControllersText(String text) throws Exception;

	String loadControllersText() throws Exception;

	String loadPagesText() throws Exception;

	void savePagesText(String text) throws Exception;

	void setSystemDescr(ControllerDescr[] controllerDescrs, UnitDescr[] unitDescrs) throws Exception;

	String loadControllerDefText(String type) throws Exception;

	void saveControllerDefText(String type, String text) throws Exception;
	
	String loadControllerUIText(String type) throws Exception;

	void saveControllerUIText(String type, String text) throws Exception;
	
	void delControllerType(String type) throws Exception;
}
