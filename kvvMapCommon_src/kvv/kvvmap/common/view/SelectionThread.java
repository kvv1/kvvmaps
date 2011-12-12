package kvv.kvvmap.common.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;

class SelectionThread {

	public interface Callback {
		void selectionChanged(ISelectable sel);
	}

	private static class Params {
		public Params(int x, int y, int w, int h, int zoom, Adapter adapter,
				PlaceMarks placemarks, Paths paths, Callback callback) {
			super();
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.zoom = zoom;
			this.adapter = adapter;
			this.placemarks = placemarks;
			this.paths = paths;
			this.callback = callback;
		}

		public final int x;
		public final int y;
		public final int w;
		public final int h;
		public final int zoom;
		public final Adapter adapter;
		public final PlaceMarks placemarks;
		public final Paths paths;
		public final Callback callback;
	}

	private ISelectable sel;

	private SelectionThread.Params params;
	private boolean cancelled;

	private SelectionRunnable r;

	private synchronized boolean cancelled() {
		return cancelled || params != null;
	}

	class SelectionRunnable implements Runnable {
		@Override
		public void run() {
			loop: for (;;) {
				SelectionThread.Params params;
				synchronized (SelectionThread.this) {
					if (SelectionThread.this.params == null) {
						r = null;
						return;
					}
					params = SelectionThread.this.params;
					SelectionThread.this.params = null;
				}

				ISelectable sel = null;

				RectX screenRect;
				int x0 = params.x - params.w / 2;
				int x1 = params.x + params.w / 2;
				int y0 = params.y - params.h / 2;
				int y1 = params.y + params.h / 2;

				double lon0 = Utils.x2lon(x0, params.zoom);
				double lonw = Utils.x2lon(x1, params.zoom) - lon0;
				double lat0 = Utils.y2lat(y1, params.zoom);
				double lath = Utils.y2lat(y0, params.zoom) - lat0;
				screenRect = new RectX(lon0, lat0, lonw, lath);

				Map<LocationX, Path> pms = new HashMap<LocationX, Path>();
				for (LocationX pm : params.placemarks.getPlaceMarks()) {
					if (cancelled())
						continue loop;
					pms.put(pm, null);
				}
				for (Path path : params.paths.getPaths()) {
					if (!path.filter(screenRect))
						continue;
					if (!path.isEnabled())
						continue;
					for (LocationX pm : path.getPlaceMarks(params.zoom)) {
						if (cancelled())
							continue loop;
						pms.put(pm, path);
					}
				}
				// System.out.println("sz= " + pms.size());

				LocationX pm = getNearest(pms.keySet(), params.x, params.y,
						params.zoom, 10);
				if (pm != null) {
					Path path = pms.get(pm);
					if (path == null)
						sel = pm;
					else
						sel = new PathSelection(path, pm);
				} else {
					sel = null;
				}

				synchronized (SelectionThread.this) {
					if (cancelled())
						continue loop;

					if (sel != null && !sel.equals(SelectionThread.this.sel)
							|| sel == null && SelectionThread.this.sel != null) {
						SelectionThread.this.sel = sel;
						final SelectionThread.Params params1 = params;
						params.adapter.execUI(new Runnable() {
							@Override
							public void run() {
								params1.callback
										.selectionChanged(SelectionThread.this.sel);
							}
						});
					}
				}
			}
		}
	}

	private LocationX getNearest(Collection<LocationX> placemarks, int x,
			int y, int zoom, long maxDist) {
		maxDist = maxDist * maxDist;
		long dist = -1;
		LocationX pmNearest = null;

		for (LocationX pm : placemarks) {
			if (cancelled())
				return null;
			long dx = pm.getX(zoom) - x;
			long dy = pm.getY(zoom) - y;
			long d = dx * dx + dy * dy;
			if ((maxDist == 0 || d < maxDist)
					&& (pmNearest == null || d < dist)) {
				pmNearest = pm;
				dist = d;
			}
		}

		return pmNearest;
	}

	public synchronized void set(int x, int y, int w, int h, int zoom,
			Adapter adapter, PlaceMarks placemarks, Paths paths,
			Callback callback) {
		this.params = new Params(x, y, w, h, zoom, adapter, placemarks, paths,
				callback);
		cancelled = false;

		if (r == null) {
			r = new SelectionRunnable();
			adapter.execBG(r);
		}
	}

	public synchronized void cancel() {
		cancelled = true;
	}
}