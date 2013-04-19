package kvv.controllers.server.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {

	public static Properties getProps(String file) throws IOException {
		Properties props = new Properties();
		FileInputStream is = new FileInputStream(file);
		props.load(is);
		is.close();
		return props;
	}

	public static String getProp(String file, String prop) {
		try {
			Properties props = getProps(file);
			return props.getProperty(prop);
		} catch (Throwable e) {
		}
		return null;
	}
}
