package kvv.aplayer.chart;

import java.util.LinkedList;
import java.util.List;

public class ChartData {
	
	public static class Item {
		public final long x;
		public final long y;
		public Item(long x, long y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	
	public final List<Item> data = new LinkedList<Item>();


	public ChartData(int maxSize) {
	}
	
	public void add(long x, long y) {
		data.add(new Item(x, y));
	}
}
