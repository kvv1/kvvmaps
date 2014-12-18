package kvv.mks.cloud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PtGrid {

	private final double step;

	private Map<Long, List<Pt>> map = new HashMap<>();

	public PtGrid(double step) {
		this.step = step;
	}

	private long hash(double x, double y, double z) {
		long x1 = (long) Math.floor(x / step);
		long y1 = (long) Math.floor(y / step);
		long z1 = (long) Math.floor(z / step);
		return hashInt(x1, y1, z1);
	}

	private long hashInt(long x, long y, long z) {
		if(x >> 20 != 0 && x >> 20 != -1)
			throw new IllegalArgumentException("" + x);
		if(y >> 20 != 0 && y >> 20 != -1)
			throw new IllegalArgumentException("" + y);
		if(z >> 20 != 0 && z >> 20 != -1)
			throw new IllegalArgumentException("" + z);
		
		long mask = (1 << 21) - 1;
		return (x & mask) + ((y & mask) << 21) + ((z & mask) << 42);
	}

	public void addAll(Collection<Pt> pts) {
		for (Pt pt : pts)
			add(pt);
	}

	public void add(Pt pt) {
		long hash = hash(pt.x, pt.y, pt.z);
		List<Pt> list = map.get(hash);
		if (list == null) {
			list = new ArrayList<>();
			map.put(hash, list);
		}
		list.add(pt);
	}

	private List<Pt> res = new ArrayList<>(300);

	public List<Pt> getNeighbours(double _x, double _y, double _z, double r) {
		res.clear();
		
		int x1 = (int) Math.floor((_x - r) / step);
		int x2 = (int) Math.floor((_x + r) / step);
		int y1 = (int) Math.floor((_y - r) / step);
		int y2 = (int) Math.floor((_y + r) / step);
		int z1 = (int) Math.floor((_z - r) / step);
		int z2 = (int) Math.floor((_z + r) / step);


		for (int z = z1; z <= z2; z++)
			for (int y = y1; y <= y2; y++)
				for (int x = x1; x <= x2; x++) {
					long hash = hashInt(x, y, z);
					List<Pt> list = map.get(hash);
					if (list != null)
						for (Pt pt : list)
							if (pt.dist(_x, _y, _z) < r)
								res.add(pt);
				}

		return res;
	}

}
