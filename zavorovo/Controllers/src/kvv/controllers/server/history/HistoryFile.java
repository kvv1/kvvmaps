package kvv.controllers.server.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

	private static PrintStream getLogStream(Date date) {
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(
					getLogFile(date), true), true, "Windows-1251");
			return ps;
		} catch (Exception e) {
			return null;
		}
	}

	static class LogItem {
		Date date;
		String text;

		public LogItem(Date date, String text) {
			this.date = date;
			this.text = text;
		}
	}

	private static final BlockingQueue<LogItem> queue = new LinkedBlockingQueue<>();
	public static volatile boolean stopped;
	private static Thread thread = new Thread() {
		{
			setPriority(MIN_PRIORITY);
			start();
		}

		public void run() {
			Logger.out.println("HistoryFile started");
			log(new Date(), null);
			long ms = 0;
			while (!stopped) {
				try {
					ms += 1000;
					LogItem logItem = queue.poll(1000, TimeUnit.MILLISECONDS);
					if (logItem != null) {
						log(logItem.date, logItem.text);
						ms = 0;
					} else if (ms > 60000) {
						Context.looper.post(new Runnable() {
							@Override
							public void run() {
								logValue("_alive_", null);
							}
						});
						ms = 0;
					}
				} catch (Exception e) {
					e.printStackTrace(Logger.out);
				}
			}
			log(new Date(), null);
			Logger.out.println("HistoryFile stopped");
		}

		private void log(Date date, String text) {
			PrintStream ps = getLogStream(date);
			ps.println(timeDF.format(date) + (text == null ? "" : " " + text));
			ps.close();
		}
	};

	public static void logValue(String register, Integer value) {
		Date date = new Date();
		if (value == null)
			queue.add(new LogItem(date, register));
		else
			queue.add(new LogItem(date, register + "=" + value));
	}

}
