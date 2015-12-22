package kvv.controllers.server.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import kvv.controllers.server.Constants;
import kvv.controllers.server.Logger;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.History;
import kvv.controllers.shared.HistoryItem;

public class HistoryFile {

	private static final DateFormat fileDF = new SimpleDateFormat("yyyy_MM_dd");
	private static final DateFormat timeDF = new SimpleDateFormat("HH:mm:ss");

	public static File getLogFile(Date date) {
		new File(Constants.historyDir).mkdir();
		return new File(Constants.historyDir + fileDF.format(date));
	}

	@SuppressWarnings("deprecation")
	public static History load(Date date) {
		History history = new History();

		try {
			File file = getLogFile(date);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "Windows-1251"));

			String line = null;
			while ((line = reader.readLine()) != null) {
				try {
					String[] parts = line.trim().split(" ");
					if (parts == null || parts.length == 0)
						continue;

					ParsePosition parsePosition = new ParsePosition(0);
					Date date1 = timeDF.parse(parts[0], parsePosition);

					if (parsePosition.getErrorIndex() == -1) {
						int seconds = date1.getHours() * 3600
								+ date1.getMinutes() * 60 + date1.getSeconds();

						if (parts.length == 1) {
							history.items.add(new HistoryItem(seconds));
						} else {
							String[] reg_value = parts[1].split("=");
							String reg = reg_value[0];
							Integer value = reg_value.length > 1 ? parseInt(reg_value[1])
									: null;
							history.items.add(new HistoryItem(seconds, reg,
									value));
						}
					}
				} catch (Exception e) {
					e.printStackTrace(Logger.out);
				}
			}
			reader.close();
		} catch (Exception e) {
			return null;
		}

		return history;
	}

	private static Integer parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Runnable aliveRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				logFile.println("_alive_");
			} catch (Exception e) {
				e.printStackTrace();
			}
			Context.looper.post(this, 60000);
		}
	};

	private static LogFile logFile = new LogFile(Constants.historyDir);
	static {
		try {
			logFile.println(null);
			Context.looper.post(aliveRunnable, 60000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stop() {
		synchronized (logFile) {
			try {
				logFile.println(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			logFile.stop();
		}
	}

	public static void logValue(String register, Integer value) {
		try {
			if (value == null)
				logFile.println(register);
			else
				logFile.println(register + "=" + value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Context.looper.remove(aliveRunnable);
		Context.looper.post(aliveRunnable, 60000);
	}

}
