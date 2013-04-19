package kvv.controllers.server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kvv.controllers.server.utils.Constants;



public class ScheduleFile {
	public boolean enabled;
	public String[] lines;

	public void save() {
		FileOutputStream scheduleOS;
		PrintWriter schwr = null;
		try {
			scheduleOS = new FileOutputStream(Constants.scheduleFile);
			schwr = new PrintWriter(scheduleOS);
			schwr.println(enabled);
			if (lines != null) {
				for (String line : lines) {
					if (line.trim().length() > 0)
						schwr.println(line);
				}
			}
		} catch (FileNotFoundException e) {
		} finally {
			schwr.close();
		}
	}

	public void load() {
		enabled = false;
		lines = null;

		FileReader schrd = null;

		try {
			schrd = new FileReader(Constants.scheduleFile);

			StringBuffer sb = new StringBuffer();
			int ch;
			while ((ch = schrd.read()) != -1)
				sb.append((char) ch);

			String[] res = sb.toString().split("[\\r\\n]+", -1);
			if (res.length > 0) {
				enabled = Boolean.parseBoolean(res[0].trim());
				lines = new String[res.length - 1];
				System.arraycopy(res, 1, lines, 0, res.length - 1);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			if (schrd != null)
				try {
					schrd.close();
				} catch (IOException e) {
				}
		}
	}

	private static DateFormat df = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);
	private static DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);

	@SuppressWarnings("deprecation")
	public static synchronized List<SetCommand> parseLine(String line,
			Date date, SetCommand[] defines) {
		if (line.trim().startsWith("#") || line.trim().length() == 0)
			return Collections.emptyList();

		String commands = null;

		ParsePosition pp = new ParsePosition(0);
		Date d = df.parse(line, pp);

		if (d != null
				&& (line.length() == pp.getIndex() || line
						.charAt(pp.getIndex()) == ' ')) {
			if (date == null
					|| (d.getYear() == date.getYear()
							&& d.getMonth() == date.getMonth()
							&& d.getDate() == date.getDate()
							&& d.getHours() == date.getHours() && d
							.getMinutes() == date.getMinutes()))
				commands = line.substring(pp.getIndex());
		} else {
			pp = new ParsePosition(0);
			d = tf.parse(line, pp);
			if (d != null
					&& (line.length() == pp.getIndex() || line.charAt(pp
							.getIndex()) == ' ')) {
				if (date == null
						|| (d != null && d.getMinutes() == date.getMinutes() && d
								.getHours() == date.getHours())) {
					commands = line.substring(pp.getIndex());
				}
			}
		}

		if (commands == null)
			return null;

		String[] cmds = commands.trim().split("\\s+", -1);

		List<SetCommand> res = new ArrayList<SetCommand>();

		l: for (int i = 0; i < cmds.length; i++) {
			for (SetCommand c : defines) {
				if (c != null && cmds[i].equals(c.name)) {
					res.add(c);
					continue l;
				}
			}
			return null;
		}

		return res;
	}

}