package kvv.controllers.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {
	private static volatile Logger logger;
	private static FileHandler fh;

	public static synchronized void stopLogger() {
		logger = null;
		if (fh != null)
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
}
