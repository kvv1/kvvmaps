package kvv.controllers.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import kvv.controllers.shared.Constants;

import com.google.gson.Gson;

public class Utils {

	private static volatile Logger logger;
	private static FileHandler fh;

	public static synchronized void stopLogger() {
		logger = null;
		fh.close();
		fh = null;
	}
	
	public static synchronized Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger("LOG");
			try {
				FileHandler fh = new FileHandler(Constants.logFile, true);
				logger.addHandler(fh);
				logger.setLevel(Level.ALL);
				SimpleFormatter formatter = new SimpleFormatter();
				fh.setFormatter(formatter);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return logger;
	}

	public static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = "\r\n";
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		return stringBuilder.toString();
	}

	{
		try {
			FileHandler fh = new FileHandler(Constants.logFile, true);
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

}
