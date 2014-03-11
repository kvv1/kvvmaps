package kvv.controllers.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
	public static <T> T fromJson(String str, Class<T> clazz) {
		return new Gson().fromJson(str, clazz);
	}

	public static <T> T jsonRead(String file, Class<T> clazz)
			throws IOException {
		Reader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file),
					"Windows-1251");
			Gson gson = new Gson();
			return gson.fromJson(reader, clazz);
		} catch (RuntimeException e) {
			throw new IOException(e);
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public static <T> void jsonWrite(String file, T src) throws Exception {
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file),
					"Windows-1251");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			writer.write(gson.toJson(src));
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "Windows-1251"));
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
		Writer wr = new OutputStreamWriter(new FileOutputStream(name),
				"Windows-1251");
		wr.write(text);
		wr.close();
	}

	public static synchronized Properties getProps(String file) {
		try {
			Properties props = new Properties();
			InputStreamReader rd = new InputStreamReader(new FileInputStream(
					file), "Windows-1251");
			props.load(rd);
			rd.close();
			return props;
		} catch (Exception e) {
			return null;
		}
	}

	public static synchronized String getProp(String file, String prop) {
		Properties props = getProps(file);
		if (props == null)
			return null;
		return props.getProperty(prop);
	}

	public static synchronized boolean changeProp(String file, String prop,
			String value) {
		try {
			Properties props = new Properties();
			try {
				InputStreamReader rd = new InputStreamReader(
						new FileInputStream(file), "Windows-1251");
				props.load(rd);
				rd.close();
			} catch (Exception e) {
			}
			props.setProperty(prop, value);
			OutputStreamWriter wr = new OutputStreamWriter(
					new FileOutputStream(file), "Windows-1251");
			props.store(wr, "");
			wr.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
