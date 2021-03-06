package kvv.kvvmap.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.placemark.PathDrawer;
import kvv.kvvmap.placemark.PathSelection;

public class Diagram {

	private Object bm;
	private int h;
	private final IPlatformView view;
	private final Adapter adapter;

	// private DiagramThread thread;

	private Runnable r;

	private class Params {
		Params(PathSelection sel, int w, int h, boolean speedProfile) {
			super();
			this.sel = sel;
			this.w = w;
			this.h = h;
			this.speedProfile = speedProfile;
		}

		final PathSelection sel;
		final int w;
		final int h;
		final boolean speedProfile;
	}

	private Params params;

	public Diagram(Adapter adapter, IPlatformView platformViewView) {
		this.view = platformViewView;
		this.adapter = adapter;
	}

	public synchronized void draw(GC gc, int y) {
		// if (r != null)
		// return;
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
						if (Diagram.this.bm != null)
							adapter.disposeBitmap(Diagram.this.bm);
						Diagram.this.bm = bm;
						view.repaint();
						return;
					}
				}

				Adapter.log("DiagramThread");

				bm = null;
				if (params.sel != null) {
					bm = adapter.allocBitmap(params.w, params.h);
					GC gc = adapter.getGC(bm);
					h = params.h;
					PathDrawer.drawDiagram(gc, params.sel, params.speedProfile);
				}
			}
		}
	}

	public synchronized void set(PathSelection sel, int w, int h,
			boolean speedProfile) {
		this.params = new Params(sel, w, h, speedProfile);
		if (r == null) {
			r = new DiagramRunnable();
			adapter.execBG(r);
		}
	}

	public synchronized void dispose() {
		if (bm != null)
			adapter.disposeBitmap(bm);
		bm = null;
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Diagram");
		super.finalize();
	}
}
