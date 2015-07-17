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
	// private final static String MOTORS_PROP_DEFAULT =
	// "c:/heliostat/motors_default.txt";

	private ValueMap<Integer> az2steps = new ValueMap<>(-60, 60,
			Environment.ANGLE_STEP);
	private ValueMap<Integer> alt2steps = new ValueMap<>(-10, 60,
			Environment.ANGLE_STEP);

	private Function azFunc;
	private Function altFunc;

	public TrajectoryImpl() {
		try {
			load(MOTORS_PROP);
		} catch (Exception e) {
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

		double[][] azData = getAzData();
		double[][] altData = getAltData();

		if (azData.length < 2 || altData.length < 2)
			return;

		azFunc = FunctionFactory.getFunction(azData[0], azData[1]);
		altFunc = FunctionFactory.getFunction(altData[0], altData[1]);
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

	public double[][] getAzData() {
		List<ValueMapEntry<Integer>> points = az2steps.getPoints();

		double[] ang = new double[points.size()];
		double[] pos = new double[points.size()];

		for (int i = 0; i < points.size(); i++) {
			ang[i] = points.get(i).arg;
			pos[i] = points.get(i).val;
		}

		return new double[][] { ang, pos };
	}

	public double[][] getAltData() {
		List<ValueMapEntry<Integer>> points = alt2steps.getPoints();

		double[] ang = new double[points.size()];
		double[] pos = new double[points.size()];

		for (int i = 0; i < points.size(); i++) {
			ang[i] = points.get(i).arg;
			pos[i] = points.get(i).val;
		}

		return new double[][] { ang, pos };
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
		ValueMap<Integer> az2steps = new ValueMap<>(-60, 60,
				Environment.ANGLE_STEP);
		ValueMap<Integer> alt2steps = new ValueMap<>(-10, 60,
				Environment.ANGLE_STEP);

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

	// public boolean saveCurrentAsDefault() throws IOException {
	// return save(MOTORS_PROP_DEFAULT);
	// }

	// public void loadDefault() throws IOException {
	// load(MOTORS_PROP_DEFAULT);
	// saveCurrent();
	// }

}
