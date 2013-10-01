package kvv.controllers.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kvv.controllers.shared.History;
import kvv.controllers.shared.HistoryItem;
import kvv.controllers.shared.Register;

public class HistoryFile {

	@SuppressWarnings("deprecation")
	public static History load(Date date) {
		History history = new History();

		try {
			File file = HistoryLogger.getLogFile(date);
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
					Register reg = Controllers.getInstance().getRegister(
							reg_value[0]);

					Integer value = reg_value.length > 0 ? Integer
							.parseInt(reg_value[1]) : null;

					ArrayList<HistoryItem> logItems = history.items.get(reg);

					if (logItems == null) {
						logItems = new ArrayList<HistoryItem>();
						history.items.put(reg, logItems);
					}

					logItems.add(new HistoryItem(seconds, value));

				} catch (Exception e) {
				}

			}
			reader.close();
		} catch (Exception e) {
			return null;
		}

		return history;
	}
}
