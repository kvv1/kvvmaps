package kvv.mks.cloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kvv.mks.Util;
import kvv.mks.rot.Rot;

public class Cloud {

	public static void main(String[] args) throws IOException {
		// new PCD(new
		// File("D:\\Users\\kvv\\Google Drive\\Mks\\mks_cloud.pcd"));
		new Cloud(new File("D:\\Users\\kvv\\Google Drive\\Mks\\skeleton.pcd"));
	}

	public List<Pt> data = new ArrayList<>();

	public Cloud(List<Pt> data) {
		this.data.addAll(data);
	}

	public Cloud(List<Pt> data, int points) {
		List<Pt> data1 = new ArrayList<>(data);
		Collections.shuffle(data1);
		this.data.addAll(data1.subList(0, points));
	}

	public Cloud(String file) throws IOException {
		this(new File(file));
	}

	public Cloud(File file) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

		try {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] ss = line.split(" ", -1);
				Pt pt = new Pt(Double.parseDouble(ss[0]),
						Double.parseDouble(ss[1]), Double.parseDouble(ss[2]));
				data.add(pt);
			}
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
			}
		}

	}

	public double minX = Integer.MAX_VALUE;
	public double maxX = Integer.MIN_VALUE;
	public double minY = Integer.MAX_VALUE;
	public double maxY = Integer.MIN_VALUE;
	public double minZ = Integer.MAX_VALUE;
	public double maxZ = Integer.MIN_VALUE;

	public double getMaxSize() {
		double max = 0;
		for (Pt pt : data) {
			max = Math.max(max,
					Math.sqrt((pt.x * pt.x + pt.y * pt.y + pt.z * pt.z)));
			minX = Math.min(minX, pt.x);
			maxX = Math.max(maxX, pt.x);
			minY = Math.min(minY, pt.y);
			maxY = Math.max(maxY, pt.y);
			minZ = Math.min(minZ, pt.z);
			maxZ = Math.max(maxZ, pt.z);
		}
		return 2 * max;
	}
/*
	public void rotate(double ax, double ay, double az) {
		double cosx = Math.cos(ax);
		double sinx = Math.sin(ax);
		double cosy = Math.cos(ay);
		double siny = Math.sin(ay);
		double cosz = Math.cos(az);
		double sinz = Math.sin(az);
		for (int i = 0; i < data.size(); i++) {
			Pt pt = data.get(i);
			pt.rotateX(cosx, sinx, pt);
			pt.rotateY(cosy, siny, pt);
			pt.rotateZ(cosz, sinz, pt);
		}
	}

	public void rotate1(double ax, double ay, double az) {
		double cosx = Math.cos(ax);
		double sinx = Math.sin(ax);
		double cosy = Math.cos(ay);
		double siny = Math.sin(ay);
		double cosz = Math.cos(az);
		double sinz = Math.sin(az);
		for (int i = 0; i < data.size(); i++) {
			Pt pt = data.get(i);
			pt.rotateZ(cosz, sinz, pt);
			pt.rotateY(cosy, siny, pt);
			pt.rotateX(cosx, sinx, pt);
		}
	}
*/
	public void rot(Rot rot) {
		for (int i = 0; i < data.size(); i++) {
			Pt pt = data.get(i);
			data.set(i,rot.apply(pt, null));
		}
	}
	
	public void translate(double x, double y, double z) {
		for (int i = 0; i < data.size(); i++) {
			Pt pt = data.get(i);
			pt.x += x;
			pt.y += y;
			pt.z += z;
		}
	}

	public void thinOut(double dist) {
		Collections.shuffle(this.data);

		List<Pt> data = new ArrayList<>();
		PtGrid grid = new PtGrid(dist * 4);

		for (Pt pt : this.data) {
			List<Pt> cand = grid.getNeighbours(pt.x, pt.y, pt.z, dist);
			if (cand.size() > 0)
				continue;
			data.add(pt);
			grid.add(pt);
		}

		this.data = data;
	}

	// public Cloud getSurface(int dim, int r) {
	// List<Pt> d = new ArrayList<>();
	//
	// double maxSz = getMaxSize();
	//
	// Pt[] zBuf = new Pt[dim * dim];
	//
	// for (Pt pt : data) {
	//
	// }
	//
	// return new Cloud(d);
	// }

	public void save(String path) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(path);
		for (Pt pt : data)
			pw.printf("%.2f %.2f %.2f\r\n", pt.x, pt.y, pt.z);
		pw.close();
	}

	public void addNoise(double d) {
		for (Pt pt : data) {
			pt.x += Util.rand(-d, d);
			pt.y += Util.rand(-d, d);
			pt.z += Util.rand(-d, d);
		}
	}

}
