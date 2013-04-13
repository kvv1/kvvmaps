package kvv.controllers.server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import kvv.controllers.client.SourcesService;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.utils.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SourcesServiceImpl extends RemoteServiceServlet implements
		SourcesService {

	@Override
	public String[] getSourceFiles() {
		return new File(Constants.ROOT, "src").list();
	}

	@Override
	public String getSource(String name) {
		try {
			return Utils.readFile(Constants.ROOT + "/src/" + name);
		} catch (IOException e) {
			return "!!! error reading file !!!";
		}
	}

	@Override
	public void setSource(String name, String text) {
		try {
			Utils.writeFile(Constants.ROOT + "/src/" + name, text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delSourceFile(String name) {
	}

	@Override
	public String getSourceFile(String controllerName) {
		Properties props = new Properties();
		try {
			FileReader fr = new FileReader(Constants.srcFile);
			props.load(fr);
			fr.close();
		} catch (IOException e) {
			return null;
		}
		return props.getProperty(controllerName);
	}

	@Override
	public void setSourceFile(String controllerName, String name) {
		Properties props = new Properties();
		try {
			FileReader fr = new FileReader(Constants.srcFile);
			props.load(fr);
			fr.close();
		} catch (IOException e) {
		}
		try {
			if (name != null)
				props.setProperty(controllerName, name);
			else
				props.remove(controllerName);

			FileWriter fw = new FileWriter(Constants.srcFile);
			props.store(fw, "");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
