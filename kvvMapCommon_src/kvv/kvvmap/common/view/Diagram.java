package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.PathDrawer;

public class Diagram {

	private Object bm;
	private int h;
	private final ICommonView view;
	private final Adapter adapter;

	//private DiagramThread thread;
	
	private Runnable r;

	private class Params {
		Params(Path path, LocationX pm, int w, int h) {
			super();
			this.path = path;
			this.pm = pm;
			this.w = w;
			this.h = h;
		}

		final Path path;
		final LocationX pm;
		final int w;
		final int h;
	}

	private Params params;

	public Diagram(Adapter adapter, ICommonView platformViewView) {
		this.view = platformViewView;
		this.adapter = adapter;
	}

	public synchronized void draw(GC gc, int y) {
		if (r != null)
			return;
		if (bm != null)
			gc.drawImage(bm, 0, gc.getHeight() - y - h);
	}

	class DiagramRunnable implements Runnable {
		@Override
		public void run() {
			Object bm = null;
			for (;;) {
				Params params;
				synchronized (Diagram.this) {
					params = Diagram.this.params;
					Diagram.this.params = null;
					if (params == null) {
						r = null;
						Diagram.this.bm = bm;
						view.repaint();
						return;
					}
				}

				bm = null;
				if (params.path != null) {
					bm = adapter.allocBitmap(params.w, params.h);
					GC gc = adapter.getGC(bm);
					h = params.h;
					PathDrawer.drawDiagram(params.path, gc, params.pm);
				}
			}
		}
	}

	public synchronized void set(Path path, LocationX pm, int w, int h) {
		this.params = new Params(path, pm, w, h / 4);
		if (r == null) {
			r = new DiagramRunnable();
			adapter.execBG(r);
		}
	}

}
