package kvv.gwtutils.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
	public static <T> T fromJson(String str, Class<T> clazz) {
		return new Gson().fromJson(str, clazz);
	}
	
	public static String toJson(Object o) {
		return new Gson().toJson(o);
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

	public static <T> void jsonWrite(String file, T src) throws IOException {
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

	static class Props {
		Properties props = new Properties();
		long time = System.currentTimeMillis();
	}

	private static final Map<String, Props> propsMap = new HashMap<String, Props>();

	public static synchronized Properties getProps(String file) {
		Props props = propsMap.get(file);
		if (props != null && System.currentTimeMillis() - props.time < 3000)
			return props.props;

		try {
			props = new Props();
			InputStreamReader rd = new InputStreamReader(new FileInputStream(
					file), "Windows-1251");
			props.props.load(rd);
			rd.close();
			propsMap.put(file, props);
			return props.props;
		} catch (Exception e) {
			propsMap.remove(file);
			return null;
		}
	}

	public static synchronized String getProp(String file, String prop) {
		Properties props = getProps(file);
		if (props == null)
			return null;
		return props.getProperty(prop);
	}

	public static synchronized String getProp(String file, String prop, String defaultValue) {
		Properties props = getProps(file);
		if (props == null)
			return null;
		return props.getProperty(prop, defaultValue);
	}

	public static synchronized Integer getPropInt(String file, String prop) {
		String val = getProp(file, prop);
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
			return null;
		}
	}

	public static synchronized boolean changeProp(String file, String prop,
			String value) {
		Properties props = getProps(file);
		if (props == null)
			props = new Properties();
		props.setProperty(prop, value);
		propsMap.remove(file);

		try {
			OutputStreamWriter wr = new OutputStreamWriter(
					new FileOutputStream(file), "Windows-1251");
			props.store(wr, "");
			wr.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static String win2utf(String str) {
		// return str;
		byte[] bytes = new byte[str.length()];
		for (int i = 0; i < str.length(); i++)
			bytes[i] = (byte) str.charAt(i);
		try {
			return new String(bytes, "Windows-1251");
		} catch (UnsupportedEncodingException e) {
			return "###";
		}
	}

	public static String utf2win(String str) {
		byte[] bytes;
		try {
			bytes = str.getBytes("Windows-1251");
			StringBuilder sb = new StringBuilder();
			for(byte b : bytes)
				sb.append((char)b);
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "xaxa";
	}

}
