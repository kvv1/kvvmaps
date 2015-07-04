package kvv.heliostat.server.trajectory;

import java.io.IOException;
import java.util.List;

import kvv.heliostat.server.Time;
import kvv.heliostat.server.Utils;
import kvv.heliostat.server.trajectory.ValueMap.ValueMapEntry;
import kvv.heliostat.shared.PtD;
import kvv.heliostat.shared.PtI;
import kvv.heliostat.shared.environment.Environment;
import kvv.heliostat.shared.spline.Function;
import kvv.heliostat.shared.spline.FunctionFactory;

public class TrajectoryImpl extends TrajectoryBase {

	// private static class MotorPoint {
	// float angle;
	// int pos;
	//
	// public MotorPoint(float angle, int pos) {
	// this.angle = angle;
	// this.pos = pos;
	// }
	// }

	private static class MotorsProps {
		List<ValueMapEntry<Integer>> azimuth;
		List<ValueMapEntry<Integer>> altitude;
	}

	private final static String MOTORS_PROP = "c:/heliostat/motors.txt";
//	private final static String MOTORS_PROP_DEFAULT = "c:/heliostat/motors_default.txt";

	private ValueMap<Integer> az2steps = new ValueMap<>(-60, 60, 2);
	private ValueMap<Integer> alt2steps = new ValueMap<>(-10, 60, 2);

	private Function azFunc;
	private Function altFunc;

	public TrajectoryImpl() {
		try {
			load(MOTORS_PROP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void addValidPos(PtI motorsPos) {
		PtD angles = new PtD(Environment.getMirrorAzimuth(Time.getDay(),
				Time.getTime()), Environment.getMirrorAltitude(Time.getDay(),
				Time.getTime()));
		az2steps.add(angles.x, motorsPos.x);
		alt2steps.add(angles.y, motorsPos.y);
		updateFuncs();
		try {
			saveCurrent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateFuncs() {
		azFunc = null;
		altFunc = null;

		List<ValueMapEntry<Integer>> azPoints = az2steps.getPoints();
		if (azPoints.size() < 2)
			return;

		List<ValueMapEntry<Integer>> altPoints = alt2steps.getPoints();
		if (altPoints.size() < 2)
			return;

		double[] azArr = new double[azPoints.size()];
		double[] azPos = new double[azPoints.size()];

		for (int i = 0; i < azPoints.size(); i++) {
			azArr[i] = azPoints.get(i).arg;
			azPos[i] = azPoints.get(i).val;
		}

		double[] altArr = new double[altPoints.size()];
		double[] altPos = new double[altPoints.size()];

		for (int i = 0; i < altPoints.size(); i++) {
			altArr[i] = altPoints.get(i).arg;
			altPos[i] = altPoints.get(i).val;
		}

		azFunc = FunctionFactory.getFunction(azArr, azPos);
		altFunc = FunctionFactory.getFunction(altArr, altPos);
	}

	@Override
	protected PtI getExpectedPos(PtI currentPos, double step) {
		if (azFunc == null || altFunc == null)
			return currentPos;

		double az = Environment.getMirrorAzimuth(Time.getDay(), Time.getTime()
				+ step);
		double alt = Environment.getMirrorAltitude(Time.getDay(),
				Time.getTime() + step);

		int x = (int) (0.5 + azFunc.value(az));
		int y = (int) (0.5 + altFunc.value(alt));

		return new PtI(x, y);
	}

	@Override
	public double[][] getPoints() {
		if (azFunc == null || altFunc == null)
			return null;

		int n = 48;

		double[] time = new double[n];
		double[] azPos = new double[n];
		double[] altPos = new double[n];

		for (int i = 0; i < n; i++) {
			double t = i * 24.0 / n;
			double az = Environment.getMirrorAzimuth(Time.getDay(), t);
			double alt = Environment.getMirrorAltitude(Time.getDay(), t);

			time[i] = t;
			azPos[i] = azFunc.value(az);
			altPos[i] = altFunc.value(alt);
		}

		return new double[][] { time, azPos, altPos };
	}

	@Override
	public void clearHistory() {
		az2steps.clear();
		alt2steps.clear();
		updateFuncs();
		try {
			saveCurrent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean save(String file) throws IOException {
		MotorsProps motorsProps = new MotorsProps();

		motorsProps.azimuth = az2steps.getPoints();
		if (motorsProps.azimuth.size() < 2)
			return false;

		motorsProps.altitude = alt2steps.getPoints();
		if (motorsProps.altitude.size() < 2)
			return false;

		Utils.jsonWrite(file, motorsProps);
		return true;
	}

	private void load(String file) throws IOException {
		ValueMap<Integer> az2steps = new ValueMap<>(-60, 60, 2);
		ValueMap<Integer> alt2steps = new ValueMap<>(-10, 60, 2);

		az2steps.clear();
		alt2steps.clear();

		MotorsProps motorsProps = Utils.jsonRead(file, MotorsProps.class);

		for (ValueMapEntry<Integer> e : motorsProps.azimuth)
			az2steps.add(e.arg, e.val);

		for (ValueMapEntry<Integer> e : motorsProps.altitude)
			alt2steps.add(e.arg, e.val);

		this.az2steps = az2steps;
		this.alt2steps = alt2steps;

		updateFuncs();
	}

	public void saveCurrent() throws IOException {
		save(MOTORS_PROP);
	}

//	public boolean saveCurrentAsDefault() throws IOException {
//		return save(MOTORS_PROP_DEFAULT);
//	}

//	public void loadDefault() throws IOException {
//		load(MOTORS_PROP_DEFAULT);
//		saveCurrent();
//	}

}
