package ants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Ants extends Canvas {

	private static final int INIT_ANTS_NUM = 500;

	static final int CELL_SIZE = 200;

	static Display display = new Display();

	private static Shell shell = new Shell(display);

	public static PlayField pf;

	Ants() {
		super(shell, SWT.DOUBLE_BUFFERED);

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (pf != null)
					for (Obj o : pf.objs)
						o._draw(e.gc);
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				super.mouseDown(e);
			}
		});

		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (pf == null) {
					pf = new PlayField(getSize().x, getSize().y);
					for (int i = 0; i < INIT_ANTS_NUM; i++) {
						double x = Math.random() * pf.w;
						double y = Math.random() * pf.h;

						RGB rgb = new RGB((int) (Math.random() * 128),
								(int) (Math.random() * 128), (int) (Math
										.random() * 128));

						pf.add(new Ant((int) x, (int) y, rgb));
					}
					// new ObjThread(0, pf.objs.size()).start();
					new ObjThread(0, pf.objs.size() / 2).start();
					new ObjThread(pf.objs.size() / 2, pf.objs.size()).start();
				} else {
					pf.createCells(getSize().x, getSize().y, CELL_SIZE);
				}
			}
		});
	}

	final Runnable r = new Runnable() {
		@Override
		public void run() {
			if (!isDisposed())
				redraw();
		}
	};

	class ObjThread extends Thread {
		private final int from;
		private final int to;

		ObjThread(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public void run() {
			while (!display.isDisposed()) {
				if (pf != null)
					for (int i = from; i < to; i++) {
						Obj o = pf.objs.get(i);
						if (o != null)
							o.step();
					}
				if (from == 0)
					display.asyncExec(r);
				try {
					sleep(30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static int n = 0;

	// Vector<Obj> passiveObjects = new Vector<Obj>();

	public static void main(String[] args) {
		shell.setLayout(new FillLayout());
		new Ants();
		loop();
	}

	private static void loop() {
		shell.open(); // open shell for user access
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose(); // must always clean up
	}

}
