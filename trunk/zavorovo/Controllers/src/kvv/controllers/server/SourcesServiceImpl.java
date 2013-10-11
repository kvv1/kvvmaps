package kvv.controllers.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import kvv.controllers.client.SourcesService;
import kvv.controllers.utils.Constants;
import kvv.controllers.utils.Utils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SourcesServiceImpl extends RemoteServiceServlet implements
		SourcesService {

	@Override
	public String[] getSourceFiles() {
		return new File(Constants.ROOT, "src").list();
	}

	@Override
	public String createSource(String name) throws Exception {
		try {
			Utils.writeFile(Constants.ROOT + "/src/" + name,
					"void main() {\r\n}\r\n");
			return "";
		} catch (IOException e) {
			throw new Exception("Error creating file");
		}
	}

	@Override
	public String getSource(String name) throws Exception {
		try {
			return Utils.readFile(Constants.ROOT + "/src/" + name);
		} catch (IOException e) {
			throw new Exception("Error reading file");
		}
	}

	@Override
	public void setSource(String name, String text) throws Exception {
		try {
			Utils.writeFile(Constants.ROOT + "/src/" + name, text);
		} catch (IOException e) {
			throw new Exception("Error writing file");
		}
	}

	@Override
	public void delSourceFile(String name) {
		new File(Constants.ROOT + "/src/" + name).delete();
	}

	@Override
	public String getSourceFileName(String controllerName) throws Exception {
		try {
			Properties props = new Properties();
			FileReader fr = new FileReader(Constants.srcFile);
			props.load(fr);
			fr.close();
			return props.getProperty(controllerName);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

}
