package kvv.mks;

import java.io.IOException;

import kvv.mks.cloud.Cloud;
import kvv.mks.opt.TargetFunc;

//10,259 -10,330 -15,140  -1,718  -1,802   1,752
//-10,500  -0,950  -1,450   0,595  -0,050  -0,150 10  1908   2701  10182 da=26,577 dd=3,469 ########## ########## t=218 

public class Test1 {

	public static void main(String[] args) throws IOException {
		Cloud data = new Cloud(TestMKS.ROOT + "/bad0.txt");

		TargetFunc targetFunc = TestMKS.getCreateFuncGrid(0.2);

		for (int i = 0; i < 200; i++) {
			State init = new State(Util.r2g(Util.rand(-20, 20)), Util.r2g(Util
					.rand(-20, 20)), Util.r2g(Util.rand(-20, 20)), Util.rand(
					-2, 2), Util.rand(-2, 2), Util.rand(-2, 2));
			TestMKS.solve(data.data, new State(), targetFunc, init);
		}
	}

}
