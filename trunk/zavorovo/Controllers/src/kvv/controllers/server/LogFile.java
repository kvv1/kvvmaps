package kvv.controllers.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.shared.Log;
import kvv.controllers.shared.LogItem;
import kvv.controllers.shared.Register;

public class LogFile {

	@SuppressWarnings("deprecation")
	public static Log load(Date date) {
		Log log = new Log();

		try {
			File file = Logger.getLogFile(date);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "Windows-1251"));

			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.trim().split(" ");
				if (parts == null || parts.length != 2)
					continue;

				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				Date date1 = df.parse(parts[0]);
				int seconds = date1.getHours() * 3600 + date1.getMinutes() * 60
						+ date1.getSeconds();

				String[] reg_value = parts[1].split("=");

				try {
					Register reg = Controllers.getRegister(reg_value[0]);

					Integer value = reg_value.length > 0 ? Integer
							.parseInt(reg_value[1]) : null;

					ArrayList<LogItem> logItems = log.items.get(reg);

					if (logItems == null) {
						logItems = new ArrayList<LogItem>();
						log.items.put(reg, logItems);
					}

					logItems.add(new LogItem(seconds, value));

				} catch (Exception e) {
				}

			}
			reader.close();
		} catch (Exception e) {
			return null;
		}

		return log;
	}

	public static void main(String[] args) {
		String[] res = "T= ".split("=");
		res = null;
	}

}
