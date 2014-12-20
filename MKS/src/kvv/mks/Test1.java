package kvv.mks;

import java.io.IOException;

import kvv.mks.cloud.Cloud;
import kvv.mks.opt.State;
import kvv.mks.opt.TargetFunc;
import kvv.mks.opt.TargetSumFuncImpl;
import kvv.mks.rot.M;
import kvv.mks.rot.Rot;

//10,259 -10,330 -15,140  -1,718  -1,802   1,752
//-10,500  -0,950  -1,450   0,595  -0,050  -0,150 10  1908   2701  10182 da=26,577 dd=3,469 ########## ########## t=218 

public class Test1 {

	public static void main(String[] args) throws IOException {

		// Pt pt = new Pt(0, 1, 1).norm();
		//
		// Quaternion q = Quaternion.fromDir(pt);
		//
		// Pt pt1 = q.inverse().apply(pt, null);
		// Pt pt2 = q.apply(pt1, null);

		Cloud scan = new Cloud(TestMKS.ROOT + "/mks_cloud.txt");
		Cloud scan1 = new Cloud(scan.data, 400);

		double ax = Util.g2r(-55);
		double ay = Util.g2r(165);
		double az = Util.g2r(55);

		State modif = new State(M.instance.rot(ax, ay, az), 0, 0, 0);
		scan1.rot(modif.rot.inverse());

		TargetFunc targetFunc = TestMKS.getCreateFuncGrid(0.2);

		TargetSumFuncImpl targetSumFuncImpl = new TargetSumFuncImpl(targetFunc,
				scan1.data);

		State maxState = findBestView(targetSumFuncImpl);

		System.out.println();

		TestMKS.solve(scan1.data, modif, targetFunc, maxState);

		/*
		 * for (int i = 0; i < 200; i++) { Rot rot = M.directions[(int)
		 * (Math.random() * M.directions.length)]; State init = new State(
		 * 
		 * , Util.rand( -2, 2), Util.rand(-2, 2), Util.rand(-2, 2)); }
		 * 
		 * 
		 * 
		 * 
		 * for (int i = 0; i < 200; i++) { State init = new
		 * State(Util.r2g(Util.rand(-20, 20)), Util.r2g(Util .rand(-20, 20)),
		 * Util.r2g(Util.rand(-20, 20)), Util.rand( -2, 2), Util.rand(-2, 2),
		 * Util.rand(-2, 2)); TestMKS.solve(data.data, new State(), targetFunc,
		 * init); }
		 */
	}

	static boolean print = false;

	static final double MAX_D = 4;
	static final double STEP_D = 1;

	private static State findBestView(TargetSumFuncImpl targetSumFuncImpl) {
		int maxVal = Integer.MIN_VALUE;
		State maxState = null;

		for (double x = -MAX_D; x <= MAX_D; x += STEP_D)
			for (double y = -MAX_D; y <= MAX_D; y += STEP_D)
				for (double z = -MAX_D; z <= MAX_D; z += STEP_D) {
					long t = System.currentTimeMillis();
					
					int n = 0;
					for (Rot rot : M.directions) {
						if (print && n % 8 == 0)
							System.out.println();

						State state = new State(rot, x, y, z);
						int val = (int) targetSumFuncImpl.getValue(state);

						if (print)
							System.out.printf("%6d ", val);

						if (val > maxVal) {
							maxVal = val;
							maxState = state;
						}

						n++;
					}
					
					//System.out.println("t=" + (System.currentTimeMillis() - t));
				}
		return maxState;
	}

}
