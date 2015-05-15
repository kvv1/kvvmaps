package kvv.heliostat.server.trajectory;

import java.util.ArrayList;
import java.util.List;

public class ValueMap<T> {

	static class ValueMapEntry<T> {
		final double arg;
		final T val;

		public ValueMapEntry(double arg, T value) {
			this.arg = arg;
			this.val = value;
		}
	}

	private final double min;
	private final double cellSize;
	private final List<ValueMapEntry<T>> array = new ArrayList<>();
	private ValueMapEntry<T> first;

	public ValueMap(double min, double max, double cellSize) {
		this.min = min;
		this.cellSize = cellSize;
		for (int i = 0; i < getCell(max); i++)
			array.add(null);

	}

	private int getCell(double arg) {
		return (int) ((arg - min) / cellSize);
	}

	public void add(double arg, T value) {
		ValueMapEntry<T> entry = new ValueMapEntry<T>(arg, value);
		array.set(getCell(arg), entry);
		if (first == null)
			first = entry;
	}

	public List<ValueMapEntry<T>> getPoints() {
		final List<ValueMapEntry<T>> l = new ArrayList<>();
		ValueMapEntry<T> last = null;

		for (ValueMapEntry<T> e : array) {
			if (e != null) {
				if (!l.isEmpty() && e.arg - last.arg < cellSize / 2)
					l.remove(l.size() - 1);
				l.add(e);
				last = e;
			}
		}

		if (first == null)
			return null;

		if (l.size() == 1 && l.get(0) != first
				&& l.get(0).arg - first.arg > cellSize / 10)
			l.add(0, first);

		return l;
	}

	public void clear() {
		for (int i = 0; i < array.size(); i++)
			array.set(i, null);
		first = null;
	}
}
