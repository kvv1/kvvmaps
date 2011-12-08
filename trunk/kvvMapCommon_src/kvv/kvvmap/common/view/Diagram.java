package kvv.kvvmap.common.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.PathDrawer;

public class Diagram {

	private Object bm;
	private final ICommonView view;
	private final Adapter adapter;

	private Path path;
	private int w;
	private int h;
	private DiagramThread thread;

	public Diagram(Adapter adapter, ICommonView platformViewView) {
		this.view = platformViewView;
		this.adapter = adapter;
	}

	public synchronized void draw(GC gc, int y) {
		if (thread != null)
			return;

		gc.drawImage(bm, 0, y - gc.getHeight());
	}

	class DiagramThread extends Thread {
		@Override
		public void run() {
			Object bm = null;
			for (;;) {
				Path path;
				int w;
				int h;
				synchronized (Diagram.this) {
					path = Diagram.this.path;
					w = Diagram.this.w;
					h = Diagram.this.h;
					Diagram.this.path = null;
					if (path == null) {
						view.repaint();
						thread = null;
						Diagram.this.bm = bm;
						return;
					}
				}
				
				bm = adapter.allocBitmap(w, h);
				GC gc = adapter.getGC(bm);
				PathDrawer.drawDiagram1(path, gc, null);
			}
		}
	}

	public synchronized void set(Path path, int w, int h) {
		this.path = path;
		if (thread == null) {
			thread = new DiagramThread();
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
	}

}
