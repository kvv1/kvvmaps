package kvv.controllers.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import kvv.controllers.server.Scheduler.ScheduleLine;
import kvv.controllers.server.utils.Utils;
import kvv.controllers.shared.Register;
import kvv.controllers.shared.RegisterSchedule;
import kvv.controllers.shared.Schedule;
import kvv.controllers.shared.ScheduleItem;
import kvv.controllers.utils.Constants;

public class ScheduleFile {
	public static void save(Schedule schedule) {
		try {
			String text = "" + schedule.enabled + "\r\n" + schedule.text;
			Utils.writeFile(Constants.scheduleFile, text);
		} catch (IOException e) {
		} finally {
		}
	}

	public static Schedule load() {
		Schedule schedule = new Schedule(false);
		schedule.date = new Date();

		try {
			String text = Utils.readFile(Constants.scheduleFile);
			schedule.text = "";
			String[] lines = text.split("[\\r\\n]+", -1);
			if (lines.length > 0) {
				schedule.enabled = Boolean.parseBoolean(lines[0].trim());
				schedule.lines = Arrays.copyOfRange(lines, 1, lines.length);

				for (String line : schedule.lines) {
					List<ScheduleLine> scheduleLine = ScheduleFile.parseLine(
							line, null);
					if (scheduleLine == null)
						continue;

					schedule.text += line + "\r\n";

					for (ScheduleLine l : scheduleLine)
						add(schedule, l.date, l.register, l.value);
				}
			}
			Properties props = Utils.getProps(Constants.scheduleProps);
			if (props != null)
				for (Register reg : schedule.map.keySet()) {
					RegisterSchedule registerSchedule = schedule.map.get(reg);
					boolean auto = Boolean.valueOf(props.getProperty(reg.name));
					registerSchedule.enabled = auto;
				}

		} catch (IOException e) {
		}

		return schedule;
	}

	@SuppressWarnings("deprecation")
	private static RegisterSchedule add(Schedule schedule, Date date,
			Register register, int value) {
		RegisterSchedule registerSchedule = schedule.map.get(register);
		if (registerSchedule == null) {
			registerSchedule = new RegisterSchedule();
			schedule.map.put(register, registerSchedule);
		}
		registerSchedule.items.add(new ScheduleItem(date.getHours() * 60
				+ date.getMinutes(), value));
		Collections.sort(registerSchedule.items,
				new Comparator<ScheduleItem>() {
					@Override
					public int compare(ScheduleItem o1, ScheduleItem o2) {
						return o1.minutes - o2.minutes;
					}
				});
		return registerSchedule;
	}

	// private static DateFormat df = DateFormat.getDateTimeInstance(
	// DateFormat.SHORT, DateFormat.SHORT);
	private static final DateFormat tf = DateFormat
			.getTimeInstance(DateFormat.SHORT);

	@SuppressWarnings("deprecation")
	public static synchronized List<ScheduleLine> parseLine(String line,
			Date date) {
		if (line.trim().startsWith("#") || line.trim().length() == 0)
			return Collections.emptyList();

		String[] fields = line.trim().split("\\s+", -1);
		if (fields.length < 1)
			return null;

		ParsePosition pp = new ParsePosition(0);
		Date d = tf.parse(fields[0], pp);
		if (d == null || pp.getIndex() != fields[0].length())
			return null;

		List<ScheduleLine> res = new ArrayList<ScheduleLine>();

		try {
			for (String cmd : Arrays.copyOfRange(fields, 1, fields.length)) {
				String[] cmdParts = cmd.split("=");
				res.add(new ScheduleLine(d, Controllers.getInstance()
						.getRegister(cmdParts[0]), Integer
						.parseInt(cmdParts[1])));
			}
		} catch (Exception e) {
			return null;
		}

		if (date == null
				|| (d.getMinutes() == date.getMinutes() && d.getHours() == date
						.getHours()))
			return res;

		return null;
	}

}