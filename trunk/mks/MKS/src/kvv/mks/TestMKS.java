package kvv.mks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kvv.mks.cloud.Cloud;
import kvv.mks.cloud.FuncGridArray;
import kvv.mks.cloud.Pt;
import kvv.mks.cloud.PtGrid;
import kvv.mks.opt.TargetFunc;
import kvv.mks.opt.TargetFuncImpl;
import kvv.mks.opt.TargetSumFunc;
import kvv.mks.opt.TargetSumFuncImpl;
import kvv.mks.opt.opt1.Solver1;
import kvv.mks.rot.M;
import kvv.mks.rot.Transform;

public class TestMKS {

	public static final String ROOT = "D:/Users/kvv/Google Drive/Mks";

	static int NTESTS = 100;

	static double THINOUT_DIST = 0.2;

	static int SCAN_POINTS = 400;

	static double OPT_DIST = 0.8;

	static int MAX_GRAD = 20;

	static double MAX_DIST = 2;

	public static void main(String[] args) throws IOException {
		Prof loading = new Prof("loading");
		Cloud scan = new Cloud(ROOT + "/mks_cloud.txt");
		loading.print();

		TargetFunc targetFunc = getCreateFuncGrid(0.2, OPT_DIST);
		//TargetFunc targetFunc = getCreateFuncGrid(0.2, OPT_DIST * 2);

		int errCnt = 0;

		for (int i = 0; i < NTESTS; i++) {
			Cloud scan1 = new Cloud(scan.data, SCAN_POINTS);

			double a = Util.g2r(MAX_GRAD);

			double ax = Util.rand(-a, a);
			double ay = Util.rand(-a, a);
			double az = Util.rand(-a, a);

			double dx = Util.rand(-MAX_DIST, MAX_DIST);
			double dy = Util.rand(-MAX_DIST, MAX_DIST);
			double dz = Util.rand(-MAX_DIST, MAX_DIST);

			Transform modif = new Transform(M.rot(ax, ay, az), dx, dy, dz);

			System.out.println(modif);

			scan1.apply1(modif);
			
			// scan1.addNoise(0.2);

			Transform state = solve(new TargetSumFuncImpl(targetFunc, scan1.data), modif, null);

			// if (Util.r2g(da) > 5) {
			// scan1.save(ROOT + "/bad" + errCnt + ".txt");
			// errCnt++;
			// }

		}

		System.out.println();
		System.out.println("errors: " + errCnt);

		System.out.println();
	}

	public static Transform solve(TargetSumFunc targetFuncSum, Transform modif,
			Transform init) {

		Solver solver;
		solver = new Solver1(Util.g2r(MAX_GRAD / 4), MAX_DIST / 4,
				targetFuncSum, init);
		// solver = new AnnealingSolver(targetFuncSum, Util.g2r(MAX_GRAD),
		// MAX_DIST);

		long t = System.currentTimeMillis();
		Transform state = solver.solve();
		t = System.currentTimeMillis() - t;

		double da = state.rot.dist(modif.rot);

		double dd = Util.dist2(modif.dx - state.dx, modif.dy - state.dy,
				modif.dz - state.dz);

		System.out.print(state + " ");
		System.out.print(solver.getAddPrint() + " ");
		System.out.printf("%6d ", (int) targetFuncSum.getValue(state));
		System.out.printf("%6d ", (int) targetFuncSum.getValue(modif));
		System.out.printf("da=%.3f dd=%.3f ", Util.r2g(da), dd);
		System.out.print(Util.getScale(10, 1, Util.r2g(da)) + " ");
		System.out.print(Util.getScale(10, 0.2, dd) + " ");
		System.out.printf("t=%3d ", (int) t);
		System.out.println();

		return state;
	}

	public static TargetFunc getCreateFuncGrid(double step, double optDist)
			throws IOException {
		File file = new File(ROOT, "grid" + (int) (step * 100) + "_"
				+ (int) (optDist * 100) + ".bin");
		if (!file.exists()) {
			Cloud model = new Cloud(ROOT + "/skeleton.txt");
			Prof thinOutProf = new Prof("thinOut");
			model = thinOut(model.data, THINOUT_DIST);
			thinOutProf.print();

			System.out.println(model.data.size() + " points in model");

			PtGrid grid = new PtGrid(optDist * 3);
			grid.addAll(model.data);
			TargetFunc targetFunc = new TargetFuncImpl(grid, optDist);


			double minX = Integer.MAX_VALUE;
			double maxX = Integer.MIN_VALUE;
			double minY = Integer.MAX_VALUE;
			double maxY = Integer.MIN_VALUE;
			double minZ = Integer.MAX_VALUE;
			double maxZ = Integer.MIN_VALUE;

			for (Pt pt : model.data) {
				minX = Math.min(minX, pt.x);
				maxX = Math.max(maxX, pt.x);
				minY = Math.min(minY, pt.y);
				maxY = Math.max(maxY, pt.y);
				minZ = Math.min(minZ, pt.z);
				maxZ = Math.max(maxZ, pt.z);
			}

			System.out.println("minX=" + minX + " maxX=" + maxX + " minY="
					+ minY + " maxY=" + maxY + " minZ=" + minZ + " maxZ="
					+ maxZ);

			TargetFunc funcGrid = new FuncGridArray(targetFunc, minX - 1,
					maxX + 1, minY - 1, maxY + 1, minZ - 1, maxZ + 1, step);

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					new FileOutputStream(file));

			objectOutputStream.writeObject(funcGrid);
			objectOutputStream.close();

			return funcGrid;
		}

		ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(file));
		TargetFunc funcGrid;
		try {
			funcGrid = (TargetFunc) objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} finally {
			objectInputStream.close();
		}

		return funcGrid;
	}

	public static Cloud thinOut(List<Pt> data1, double dist) {
		data1 = new ArrayList<>(data1);
		Collections.shuffle(data1);

		List<Pt> data = new ArrayList<>();
		PtGrid grid = new PtGrid(dist * 4);

		for (Pt pt : data1) {
			List<Pt> cand = grid.getNeighbours(pt.x, pt.y, pt.z, dist);
			if (cand.size() > 0)
				continue;
			data.add(pt);
			grid.add(pt);
		}

		return new Cloud(data);
	}

}
