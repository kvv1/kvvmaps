package kvv.heliostat.engine;

import java.io.IOException;
import java.util.ArrayList;

import kvv.stdutils.Utils;

public class AngleStepTable {

	ArrayList<double[]> array = new ArrayList<>();
	private final double min;
	private final double cellSize;
	private final String path;

	public AngleStepTable(double min, double max, double cellSize, String path) {
		this.path = path;
		this.min = min;
		this.cellSize = cellSize;
		for (int i = 0; i < getCell(max); i++)
			array.add(null);
		try {
			load(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getCell(double arg) {
		return (int) ((arg - min) / cellSize);
	}

	public void add(double angle, double pos) {
		array.set(getCell(angle), new double[] { angle, pos });
		try {
			save(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class Wrapper {
		ArrayList<double[]> data;
	}

	void load(String file) throws IOException {
		array = Utils.jsonRead(file, Wrapper.class).data;
	}

	void save(String file) throws IOException {
		Wrapper wr = new Wrapper();
		wr.data = array;
		Utils.jsonWrite(file, wr);
	}

	public double[][] getData() {
		int cnt = 0;
		for (double[] p : array)
			if (p != null)
				cnt++;

		double[] ang = new double[cnt];
		double[] pos = new double[cnt];
		
		int idx = 0;
		for (double[] p : array)
			if (p != null) {
				ang[idx] = p[0];
				pos[idx] = p[1];
				idx++;
			}
				
		return new double[][] { ang, pos };
	}

	public void clear() {
		for (int i = 0; i < array.size(); i++)
			array.set(i, null);
		try {
			save(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double get(double x) {
		double[][] data = getData();
		Function func = FunctionFactory.getFunction(data[0], data[1]);
		return func.value(x);
	}

}
