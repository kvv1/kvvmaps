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
import kvv.mks.rot.Transform;

public class Cloud {

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

	public void relToAbs(Transform transform) {
		for (int i = 0; i < data.size(); i++) {
			Pt pt = data.get(i);
			data.set(i, transform.relToAbs(pt, null));
		}
	}

	public void absToRel(Transform transform) {
		for (int i = 0; i < data.size(); i++) {
			Pt pt = data.get(i);
			data.set(i, transform.absToRel(pt, null));
		}
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
