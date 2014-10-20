package kvv.goniometer;

import java.util.LinkedHashMap;

public class SensorData {
	public int e;
	public int x;
	public int y;
	public int t;
	public LinkedHashMap<Integer, Integer> spectrum = new LinkedHashMap<>();
}
