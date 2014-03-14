package kvv.controllers.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("file")
public interface FileService  extends RemoteService {
	String get(String path) throws Exception;
	void set(String path, String text) throws Exception;
}
