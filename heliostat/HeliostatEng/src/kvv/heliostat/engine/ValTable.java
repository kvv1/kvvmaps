package kvv.heliostat.engine;

public class ValTable {
	
	private int[] values;
	
	int lastVal;
	double lastArg = Double.NaN;
	
	
	public ValTable(int first, int last) {
		values = new int[last - first + 1];
	}
	
	public void add(double arg, int val) {
		if(lastArg == Double.NaN) {
			lastArg = arg;
			lastVal = val;
		} else if(arg > lastArg){
			int idx0 = (int) lastArg;
			int idx1 = (int)arg;
			
			
			
		}
	}

}
