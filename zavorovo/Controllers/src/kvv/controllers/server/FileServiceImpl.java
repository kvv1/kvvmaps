package kvv.controllers.server;

import kvv.controllers.client.FileService;
import kvv.controllers.server.history.HistoryFile;
import kvv.gwtutils.server.login.LoginServlet;
import kvv.gwtutils.server.login.UserService;
import kvv.stdutils.Utils;

@SuppressWarnings("serial")
public class FileServiceImpl extends LoginServlet implements FileService {

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
			HistoryFile.logUserAction(LoginServlet.getUserName(),
					"Сохранение файла " + path);
			Utils.writeFile(Constants.ROOT + path, text);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	protected UserService getUserService() {
		return ContextListener.userService;
	}

}
