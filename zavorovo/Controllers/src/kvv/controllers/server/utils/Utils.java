package kvv.controllers.server.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import com.google.gson.Gson;

public class Utils {
	public static <T> T jsonRead(String file, Class<T> clazz) throws Exception {
		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file),
					"Windows-1251");
			Gson gson = new Gson();
			return gson.fromJson(reader, clazz);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append("\r\n");
		}
		reader.close();
		return stringBuilder.toString();
	}

	public static void writeFile(String name, String text) throws IOException {
		FileWriter wr = new FileWriter(name);
		wr.write(text);
		wr.close();
	}

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
