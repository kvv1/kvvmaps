package kvv.controllers.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kvv.controllers.server.Scheduler.Command;
import kvv.controllers.utils.Constants;

public class ScheduleFile {
	public boolean enabled;
	public String[] lines;

	public void save() {
		PrintWriter schwr = null;
		try {
			Writer wr = new OutputStreamWriter(new FileOutputStream(
					Constants.scheduleFile), "Windows-1251");
			schwr = new PrintWriter(wr);
			schwr.println(enabled);
			if (lines != null) {
				for (String line : lines) {
					if (line.trim().length() > 0)
						schwr.println(line);
				}
			}
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} finally {
			schwr.close();
		}
	}

	public void load() {
		enabled = false;
		lines = null;

		Reader schrd = null;

		try {
			schrd = new InputStreamReader(new FileInputStream(
					Constants.scheduleFile), "Windows-1251");

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
	public static synchronized List<Command> parseLine(String line, Date date) {
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

		List<Command> res = new ArrayList<Command>();

		try {
			for (String cmd : cmds) {
				String[] cmdParts = cmd.split("=");
				res.add(new Command(Controllers.getRegister(cmdParts[0]),
						Integer.parseInt(cmdParts[1])));
			}
		} catch (Exception e) {
			return null;
		}

		return res;
	}

}