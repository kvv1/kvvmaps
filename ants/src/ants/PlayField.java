package ants;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class PlayField {

	Vector<Obj> objs = new Vector<Obj>();

	public PlayField(int w, int h) {
		createCells(w, h, Ants.CELL_SIZE);
	}

	void add(Obj o) {
		objs.add(o);
		moveObj(o);
	}

	public Set<Obj>[][] cells;

	int w;
	int h;

	int cw;
	int ch;

	private int cellSize;

	@SuppressWarnings("unchecked")
	void createCells(int w, int h, int cellSize) {
		this.w = w;
		this.h = h;
		this.cellSize = cellSize;
		cw = w / cellSize + 1;
		ch = h / cellSize + 1;
		cells = new Set[cw][ch];

		for (int x = 0; x < cw; x++)
			for (int y = 0; y < ch; y++)
				cells[x][y] = new HashSet<Obj>();

		for (Obj o : objs) {
			moveObj(o);
		}
	}

	void moveObj(Obj o) {
		int x = (int) o.x;
		int y = (int) o.y;
		if (x < 0)
			x = 0;
		if (x >= w)
			x = w - 1;
		if (y < 0)
			y = 0;
		if (y >= h)
			y = h - 1;

		int cellx = x / cellSize;
		int celly = y / cellSize;

		if (cellx != o.cellx || celly != o.celly) {
			Set<Obj> v = cells[o.cellx][o.celly];
			synchronized (v) {
				v.remove(o);
			}

			Set<Obj> v1 = cells[cellx][celly];
			synchronized (v1) {
				v1.add(o);
			}
			o.cellx = cellx;
			o.celly = celly;
		}
	}

}
