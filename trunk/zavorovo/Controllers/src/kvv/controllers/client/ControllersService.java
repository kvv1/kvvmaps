package kvv.controllers.client;

import java.util.Map;

import kvv.controllers.register.AllRegs;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.shared.PageDescr;
import kvv.controllers.shared.SystemDescr;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("controllers")
public interface ControllersService extends RemoteService {
	int getReg(int addr, int reg) throws Exception;

	void setReg(int addr, int reg, int val) throws Exception;

	AllRegs getRegs(int addr) throws Exception;

//	ControllerDescr[] getControllers() throws Exception;
//	PageDescr[] getPages() throws Exception;

	SystemDescr getSystemDescr()  throws Exception;

	String upload(int addr, String name);

	void vmInit(int addr) throws Exception;

	void saveControllersText(String text) throws Exception;

	String loadControllersText() throws Exception;

	void savePageScript(String pageName, String script) throws Exception;

	String loadPagesText() throws Exception;

	void savePagesText(String text) throws Exception;

	void enableScript(String pageName, boolean b) throws Exception;

	Map<String, String> getVMErrors();
}
