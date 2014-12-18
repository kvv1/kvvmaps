package kvv.mks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import kvv.mks.cloud.Cloud;
import kvv.mks.cloud.FuncGridArray;
import kvv.mks.cloud.Pt;
import kvv.mks.cloud.PtGrid;
import kvv.mks.opt.TargetFunc;
import kvv.mks.opt.TargetFuncImpl;
import kvv.mks.opt.TargetSumFunc;
import kvv.mks.opt.TargetSumFuncImpl;
import kvv.mks.opt.annealing.AnnealingSolver;
import kvv.mks.opt.opt1.Solver1;

public class TestMKS {

	public static final String ROOT = "D:/Users/kvv/Google Drive/Mks";

	static int N = 100;

	static double THINOUT_DIST = 0.2;

	static int SCAN_POINTS = 400;

	static double OPT_DIST = 0.8;

	static int MAX_GRAD = 20;

	static double MAX_DIST = 2;

	public static void main(String[] args) throws IOException {
		Prof loading = new Prof("loading");
		Cloud scan = new Cloud(ROOT + "/mks_cloud.txt");
		loading.print();

		TargetFunc targetFunc = getCreateFuncGrid(0.2);

		int errCnt = 0;

		for (int i = 0; i < N; i++) {
			Cloud scan1 = new Cloud(scan.data, SCAN_POINTS);

			double a = Util.g2r(MAX_GRAD);

			State modif = new State(Util.rand(-a, a), Util.rand(-a, a),
					Util.rand(-a, a), Util.rand(-MAX_DIST, MAX_DIST),
					Util.rand(-MAX_DIST, MAX_DIST), Util.rand(-MAX_DIST,
							MAX_DIST));

			System.out.println(modif);

			scan1.translate(-modif.dx, -modif.dy, -modif.dz);
			scan1.rotate1(-modif.ax, -modif.ay, -modif.az);
			scan1.addNoise(0.2);

			State state = solve(scan1.data, modif, targetFunc, null);

//			if (Util.r2g(da) > 5) {
//				scan1.save(ROOT + "/bad" + errCnt + ".txt");
//				errCnt++;
//			}

		}

		System.out.println();
		System.out.println("errors: " + errCnt);

		System.out.println();
	}

	
	public static State solve(List<Pt> data, State modif, TargetFunc targetFunc, State init) {
		TargetSumFunc targetFuncSum = new TargetSumFuncImpl(targetFunc, data);

		Solver solver;
		solver = new Solver1(Util.g2r(MAX_GRAD / 4), MAX_DIST / 4,
				targetFuncSum, init);
		//solver = new AnnealingSolver(targetFuncSum, Util.g2r(MAX_GRAD), MAX_DIST);

		long t = System.currentTimeMillis();
		State state = solver.solve();
		t = System.currentTimeMillis() - t;

		double da = Util.dist2(modif.ax - state.ax, modif.ay - state.ay,
				modif.az - state.az);

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

	public static TargetFunc getCreateFuncGrid(double step) throws IOException {
		File file = new File(ROOT, "grid" + (int) (step * 100) + ".bin");
		if (!file.exists()) {
			Cloud model = new Cloud(ROOT + "/skeleton.txt");
			Prof thinOutProf = new Prof("thinOut");
			model.thinOut(THINOUT_DIST);
			thinOutProf.print();

			System.out.println(model.data.size() + " points in model");

			PtGrid grid = new PtGrid(OPT_DIST * 3);
			grid.addAll(model.data);

			TargetFunc targetFunc = new TargetFuncImpl(grid, OPT_DIST);

			model.getMaxSize();

			System.out.println("minX=" + model.minX + " maxX=" + model.maxX
					+ " minY=" + model.minY + " maxY=" + model.maxY + " minZ="
					+ model.minZ + " maxZ=" + model.maxZ);

			TargetFunc funcGrid = new FuncGridArray(targetFunc, model.minX - 1,
					model.maxX + 1, model.minY - 1, model.maxY + 1,
					model.minZ - 1, model.maxZ + 1, step);

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
}
