package kvv.controllers.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import kvv.controllers.client.FileService;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

@SuppressWarnings("serial")
public class FileServiceImpl extends RemoteServiceServlet implements
		FileService {

	@Override
	public String get(String path) throws Exception {
		if (path.contains(".."))
			throw new Exception("wrong file path");
		try {
			return Utils.readFile(Constants.ROOT + path);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void set(String path, String text) throws Exception {
		if (path.contains(".."))
			throw new Exception("wrong file path");
		try {
			Utils.writeFile(Constants.ROOT + path, text);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

}
