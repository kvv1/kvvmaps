package kvv.heliostat.server.trajectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kvv.heliostat.server.Time;
import kvv.heliostat.server.Utils;
import kvv.heliostat.shared.PtI;
import kvv.heliostat.shared.spline.Function;
import kvv.heliostat.shared.spline.FunctionFactory;

import com.google.gson.Gson;

public class TrajectoryImpl_2 extends TrajectoryBase {

	private static final String HISTORY_FILE = "c:/heliostat/history.txt";
	private static final String TODAY_FILE = "c:/heliostat/today.txt";

	private final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");

	private final static int HISTORY_DAYS = 30;
	private final static int HISTORY_POINTS = 96;
	private final static int HISTORY_ACURACY_SEC = 10;
	private final static int HISTORY_POINTS_TO_INTERPOLATE = 1/* hours */* HISTORY_POINTS / 24;

	private PtI[][] history = new PtI[HISTORY_DAYS][HISTORY_POINTS];
	private PtI[] workingTable = new PtI[HISTORY_POINTS];
	private Calendar lastDay;

	private static class Line {
		static class LineItem {
			float time;
			int[] pos;
		}

		private String date;
		private ArrayList<LineItem> items = new ArrayList<>();

		PtI[] getPts() {
			PtI[] pts = new PtI[HISTORY_POINTS];
			for (LineItem i : items) {
				int idx = time2idx(i.time);
				double t0 = idx2time(idx);
				if (idx < HISTORY_POINTS
						&& Math.abs(i.time - t0) * 3600 < HISTORY_ACURACY_SEC) {
					pts[idx] = new PtI(i.pos[0], i.pos[1]);
				}
			}
			return pts;
		}

		public void add(int i, int az, int alt) {
			LineItem item = new LineItem();
			item.time = (float) idx2time(i);
			item.pos = new int[] { az, alt };
			items.add(item);
		}
	}

	public TrajectoryImpl_2() {
		readHistory();
	}

	public void readHistory() {
		history = new PtI[HISTORY_DAYS][HISTORY_POINTS];

		Calendar today = Time.getCalendar();
		lastDay = today;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(HISTORY_FILE));

			String line;
			while ((line = reader.readLine()) != null)
				if (!line.trim().isEmpty()) {
					Line l = new Gson().fromJson(line, Line.class);
					Calendar c = Calendar.getInstance();
					c.setTime(format1.parse(l.date));
					int days = dayDiff(today, c);
					if (days > 0 && days < HISTORY_DAYS)
						history[days] = l.getPts();
				}

		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}

		try {
			Line l = Utils.jsonRead(TODAY_FILE, Line.class);
			Calendar c = Calendar.getInstance();
			c.setTime(format1.parse(l.date));
			int days = dayDiff(today, c);
			if (days == 0) {
				history[0] = l.getPts();
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}

		createWorkingTable();
	}

	private void createWorkingTable() {
		workingTable = new PtI[HISTORY_POINTS];

		PtI[][] history = new PtI[HISTORY_DAYS][HISTORY_POINTS];

		for (int i = 0; i < HISTORY_DAYS; i++)
			for (int j = 0; j < HISTORY_POINTS; j++)
				history[i][j] = this.history[i][j];

		for (int d = 0; d < HISTORY_DAYS; d++) {
			Integer lastP = null;
			PtI[] pts = history[d];
			for (int p = 0; p < HISTORY_POINTS; p++) {
				if (pts[p] != null) {
					if (lastP != null
							&& p - lastP <= HISTORY_POINTS_TO_INTERPOLATE) {
						for (int i = lastP + 1; i < p; i++) {
							int x = (int) FunctionFactory.linInterpol(lastP,
									pts[lastP].x, p, pts[p].x, i);
							int y = (int) FunctionFactory.linInterpol(lastP,
									pts[lastP].y, p, pts[p].y, i);
							pts[i] = new PtI(x, y);
						}
					}
					lastP = p;
				}
			}
		}

		for (int p = 0; p < HISTORY_POINTS; p++) {
			if (history[0][p] != null) {
				workingTable[p] = history[0][p];
				continue;
			}

			Integer first = null;
			Integer last = null;
			for (int d = 1; d < HISTORY_DAYS; d++) {
				if (history[d][p] != null) {
					if (first == null)
						first = d;
					last = d;
				}
			}

			if (first != null) {
				if (last > first) {
					int az = history[first][p].x + (0 - first)
							* (history[last][p].x - history[first][p].x)
							/ (last - first);
					int alt = history[first][p].y + (0 - first)
							* (history[last][p].y - history[first][p].y)
							/ (last - first);
					workingTable[p] = new PtI(az, alt);
				} else if (first < 5) {
					workingTable[p] = history[first][p];
				}

			}
		}
	}

	public void addValidPos(PtI motorsPos) {
		checkDay();

		double time = Time.getTime();
		int idx = time2idx(time);
		double time0 = idx2time(idx);
		if (Math.abs(time - time0) * 3600 < HISTORY_ACURACY_SEC) {
			history[0][idx] = motorsPos;
			workingTable[idx] = motorsPos;
			saveToday(TODAY_FILE, false);
		}
	}

	private void saveToday(String file, boolean append) {
		FileWriter wr = null;
		try {
			wr = new FileWriter(file, append);

			Line l = new Line();
			l.date = format1.format(lastDay.getTime());

			for (int i = 0; i < HISTORY_POINTS; i++) {
				PtI pt = history[0][i];
				if (pt != null)
					l.add(i, pt.x, pt.y);
			}

			wr.write(new Gson().toJson(l) + "\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wr != null)
				try {
					wr.close();
				} catch (IOException e) {
				}
		}
	}

	public void checkDay() {
		Calendar today = Time.getCalendar();
		if (!datesEqual(lastDay, today)) {
			saveToday(HISTORY_FILE, true);
			readHistory();
		}
	}

	@Override
	public double[][] getPoints() {
		checkDay();

		int n = 0;

		for (int i = 0; i < workingTable.length; i++)
			if (workingTable[i] != null)
				n++;

		if (n == 0)
			return null;

		double[] time = new double[n];
		double[] azPos = new double[n];
		double[] altPos = new double[n];

		int k = 0;

		for (int i = 0; i < workingTable.length; i++)
			if (workingTable[i] != null) {
				time[k] = idx2time(i);
				azPos[k] = workingTable[i].x;
				altPos[k] = workingTable[i].y;
				k++;
			}

		return new double[][] { time, azPos, altPos };
	}

	@Override
	protected PtI getExpectedPos(PtI currentPos, double step) {
		double[][] points = getPoints();
		if (points == null || points.length < 2)
			return currentPos;

		Function azFunc = FunctionFactory.getFunction(points[0], points[1]);
		Function altFunc = FunctionFactory.getFunction(points[0], points[2]);

		int x = (int) (0.5 + azFunc.value(Time.getTime() + step));
		int y = (int) (0.5 + altFunc.value(Time.getTime() + step));

		return new PtI(x, y);
	}

	private static int time2idx(double time) {
		return (int) (time * HISTORY_POINTS / 24 + 0.5);
	}

	private static double idx2time(int idx) {
		return idx * 24.0 / HISTORY_POINTS;
	}

	private static int dayDiff(Calendar c1, Calendar c2) {
		return (int) ((c1.getTimeInMillis() - c2.getTimeInMillis()) / (24 * 60 * 60 * 1000));
	}

	private static boolean datesEqual(Calendar c1, Calendar c2) {
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
	}

	@Override
	public void clearHistory() {
		new File(HISTORY_FILE).delete();
		new File(TODAY_FILE).delete();
		readHistory();
	}

	public static void main(String[] args) {
		System.out.println("" + (0.25f * 32));
	}
}
