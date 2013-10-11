package kvv.controllers.history;

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

import kvv.controllers.history.shared.History;
import kvv.controllers.history.shared.HistoryItem;
import kvv.controllers.utils.Constants;

public class HistoryFile {

	private static final DateFormat fileDF = new SimpleDateFormat("yyyy_MM_dd");

	public static File getLogFile(Date date) {
		new File(Constants.ROOT + "/history").mkdir();
		return new File(Constants.ROOT + "/history/" + fileDF.format(date));
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

						Integer value = reg_value.length > 0 ? Integer
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
					HistoryFile.getLogFile(date), true), true, "Windows-1251");
			return ps;
		} catch (Exception e) {
			return null;
		}
	}

	private static DateFormat timeDF = new SimpleDateFormat("HH:mm:ss");

	public static void logValue(Date date, String register, Integer value) {
		PrintStream ps = getLogStream(date);
		if (register == null)
			ps.println(timeDF.format(date));
		else if (value == null)
			ps.println(timeDF.format(date) + " " + register);
		else {
			ps.println(timeDF.format(date) + " " + register + "=" + value);
		}
		ps.close();

	}

}
