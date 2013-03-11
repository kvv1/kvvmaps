package kvv.controllers.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("log")
public interface LogService extends RemoteService{
	String getLog();
}
