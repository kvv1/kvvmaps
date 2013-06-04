package kvv.controllers.controller;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import kvv.controllers.utils.Constants;

public class BusLogger {
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
				fh = new FileHandler(Constants.rs485LogFile, true);
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
