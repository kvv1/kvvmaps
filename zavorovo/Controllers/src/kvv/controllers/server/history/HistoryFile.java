package kvv.controllers.server.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import kvv.controllers.server.Constants;
import kvv.controllers.shared.History;
import kvv.controllers.shared.HistoryItem;

public class HistoryFile {

	private static final DateFormat fileDF = new SimpleDateFormat("yyyy_MM_dd");

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
				String[] parts = line.trim().split(" ");
				if (parts == null || parts.length == 0)
					continue;

				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				Date date1 = df.parse(parts[0]);
				int seconds = date1.getHours() * 3600 + date1.getMinutes() * 60
						+ date1.getSeconds();

				if (parts.length == 1) {
					for (ArrayList<HistoryItem> logItems : history.items
							.values())
						logItems.add(new HistoryItem(seconds, null));
				} else {
					String[] reg_value = parts[1].split("=");

					try {
						String reg = reg_value[0];

						Integer value = reg_value.length > 1 ? Integer
								.parseInt(reg_value[1]) : null;

						ArrayList<HistoryItem> logItems = history.items
								.get(reg);

						if (logItems == null) {
							logItems = new ArrayList<HistoryItem>();
							history.items.put(reg, logItems);
						}

						logItems.add(new HistoryItem(seconds, value));

					} catch (Exception e) {
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			return null;
		}

		return history;
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

	private static DateFormat timeDF = new SimpleDateFormat("HH:mm:ss");

	static class LogItem {
		Date date;
		String register;
		Integer value;

		public LogItem(Date date, String register, Integer value) {
			this.date = date;
			this.register = register;
			this.value = value;
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
			System.out.println("HistoryFile started");
			while (!stopped) {
				try {
					LogItem logItem = queue.poll(1000, TimeUnit.MILLISECONDS);
					if (logItem != null) {
						//System.out.println(logItem.date);
						PrintStream ps = getLogStream(logItem.date);
						if (logItem.register == null)
							ps.println(timeDF.format(logItem.date));
						else if (logItem.value == null)
							ps.println(timeDF.format(logItem.date) + " "
									+ logItem.register);
						else
							ps.println(timeDF.format(logItem.date) + " "
									+ logItem.register + "=" + logItem.value);
						ps.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("HistoryFile stopped");
		}
	};

	public static void logValue(Date date, String register, Integer value) {
		queue.add(new LogItem(date, register, value));
		// PrintStream ps = getLogStream(date);
		// if (register == null)
		// ps.println(timeDF.format(date));
		// else if (value == null)
		// ps.println(timeDF.format(date) + " " + register);
		// else {
		// ps.println(timeDF.format(date) + " " + register + "=" + value);
		// }
		// ps.close();
	}

}
