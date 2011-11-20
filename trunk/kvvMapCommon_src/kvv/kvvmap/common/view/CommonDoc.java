package kvv.kvvmap.common.view;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.LongSet;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maps.MapDescr;
import kvv.kvvmap.common.maptiles.MapTiles;
import kvv.kvvmap.common.pacemark.IPlaceMarksListener;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.pathtiles.PathTiles;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileId;

public final class CommonDoc implements IPlaceMarksListener {

	public static boolean debugDraw = false;

	private int zoom = Utils.MIN_ZOOM;

	private final ICommonView view;

	private PointInt centerXY;

	private InfoLevel infoLevel = InfoLevel.HIGH;

	private final Environment envir;

	private final MapTiles mapTiles;
	private final PathTiles pathTiles;

	public CommonDoc(ICommonView view, Environment envir) {
		this.envir = envir;
		this.view = view;
		envir.placemarks.setDoc(this);
		envir.paths.setDoc(this);
		this.mapTiles = new MapTiles(envir.adapter, envir.maps,
				Adapter.MAP_TILES_CACHE_SIZE) {
			@Override
			protected void loaded(Tile tile) {
				repaint();
			}

			// @Override
			// protected int compare(long t1, long t2) {
			// return compareTiles(t1, t2);
			// }
		};
		this.pathTiles = new PathTiles(envir.adapter, envir.placemarks,
				envir.paths, Adapter.PATH_TILES_CACHE_SIZE) {

			@Override
			protected void loaded(Tile tile) {
				repaint();
			}

			// @Override
			// protected int compare(long t1, long t2) {
			// return compareTiles(t1, t2);
			// }

			@Override
			protected InfoLevel getInfoLevel() {
				return infoLevel;
			}

			@Override
			protected ISelectable getSelAsync() {
				return CommonDoc.this.getSelAsync();
			}
		};
	}

	public void zoomOut() {
		if (zoom > Utils.MIN_ZOOM)
			setZoom(zoom - 1);
	}

	public void zoomIn() {
		if (zoom < Utils.MAX_ZOOM)
			setZoom(zoom + 1);
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		LocationX loc = getLocation();
		this.zoom = zoom;
		animateTo(loc);
		repaint();
	}

	public void animateTo(LocationX loc, int dx, int dy) {
		envir.adapter.assertUIThread();
		if (loc.getLatitude() > 85 || loc.getLatitude() < -85)
			return;
		int x = (int) (Utils.lon2x(loc.getLongitude(), zoom));
		int y = (int) (Utils.lat2y(loc.getLatitude(), zoom));
		centerXY = new PointInt(x - dx, y - dy);
		repaint();
	}

	public void animateTo(LocationX loc) {
		animateTo(loc, 0, 0);
	}

	public PointInt getCenterXY() {
		return centerXY;
	}

	public LocationX getLocation() {
		return getLocation(0, 0);
	}

	public LocationX getLocation(int dx, int dy) {
		envir.adapter.assertUIThread();
		return new LocationX(Utils.x2lon(centerXY.x + dx, zoom), Utils.y2lat(
				centerXY.y + dy, zoom));
	}

	public void animateBy(PointInt offset) {
		envir.adapter.assertUIThread();
		selectionThread.cancel();
		int x = centerXY.x + offset.x;
		int y = centerXY.y + offset.y;
		double lat = Utils.y2lat(y, zoom);
		if (lat > 85 || lat < -85)
			return;
		centerXY = new PointInt(x, y);
		repaint();
	}

	class LastDrawParams {
		public int w;
		public int h;
		public int zoom;
		public PointInt center;
		public LongSet tiles = new LongSet();
	}

	private final LastDrawParams lastDraw = new LastDrawParams();

	public void draw(GC gc) {
		int w = gc.getWidth();
		int h = gc.getHeight();

		synchronized (lastDraw) {
			if (w != lastDraw.w || h != lastDraw.h
					|| centerXY != lastDraw.center || zoom != lastDraw.zoom) {
				mapTiles.stopLoading();
				pathTiles.stopLoading();
			}

			lastDraw.w = w;
			lastDraw.h = h;
			lastDraw.zoom = zoom;
			lastDraw.center = centerXY;
			lastDraw.tiles.clear();
		}

		int x0 = centerXY.x - w / 2;
		int y0 = centerXY.y - h / 2;

		int nx0 = x0 / Adapter.TILE_SIZE;
		int ny0 = y0 / Adapter.TILE_SIZE;

		int nx1 = (x0 + w - 1) / Adapter.TILE_SIZE;
		int ny1 = (y0 + h - 1) / Adapter.TILE_SIZE;

		synchronized (mapTiles) {
			synchronized (pathTiles) {
				for (int nx = nx0; nx <= nx1; nx++) {
					for (int ny = ny0; ny <= ny1; ny++) {
						long id = TileId.get(nx, ny, zoom);

						int x = (nx * Adapter.TILE_SIZE) - x0;
						int y = (ny * Adapter.TILE_SIZE) - y0;

						Tile tile = mapTiles.getTile(id, centerXY);
						if (tile != null)
							tile.draw(gc, x, y);

						if (getInfoLevel().ordinal() > 0) {
							tile = pathTiles.getTile(id, centerXY);
							if (tile != null)
								tile.draw(gc, x, y);
						}
						synchronized (lastDraw) {
							lastDraw.tiles.add(id);
						}
					}
				}
			}
		}
	}

	public boolean isMultiple() {
		Tile tile = getCenterTile();
		if (tile == null)
			return false;
		return tile.isMultiple();
	}

	public List<MapDescr> getCenterMaps() {
		Tile tile = getCenterTile();
		if (tile == null)
			return Collections.emptyList();
		return tile.content.maps;
	}

	private Tile getCenterTile() {
		envir.adapter.assertUIThread();
		int nxC = centerXY.x / Adapter.TILE_SIZE;
		int nyC = centerXY.y / Adapter.TILE_SIZE;
		long id = TileId.get(nxC, nyC, zoom);
		Tile tile = mapTiles.getTile(id, centerXY);
		return tile;
	}

	private void repaint() {
		envir.adapter.assertUIThread();
		view.repaint();
	}

	public void reorderMaps() {
		Tile tile = getCenterTile();
		if (tile != null && tile.isMultiple()) {
			mapTiles.reorder(tile);
			repaint();
		}
	}

	public String getTopMap() {
		envir.adapter.assertUIThread();
		Tile tile = getCenterTile();
		if (tile == null)
			return "<No map>";
		return tile.content.maps.getFirst().getName();
	}

	public void setTopMap(String map) {
		mapTiles.setTopMap(map);
	}

	public void dispose() {
		pathTiles.dispose();
		mapTiles.dispose();
	}

	@Override
	public void onPathTileChanged(long id) {
		envir.adapter.assertUIThread();
		if (pathTiles != null) {
			pathTiles.setInvalid(id);
			synchronized (lastDraw) {
				if (lastDraw.tiles.contains(id)) {
					repaint();
				}
			}
		}
	}

//	private final LongHashMap<Runnable> m1 = new LongHashMap<Runnable>();
//
//	@Override
//	public void onPathTileChangedAsync(final long id) {
//		envir.adapter.assertUIThread();
//		Runnable r;
//		synchronized (m1) {
//			r = m1.get(id);
//			if (r == null) {
//				r = new Runnable() {
//					@Override
//					public void run() {
//						onPathTileChanged(id);
//					}
//				};
//				m1.put(id, r);
//			}
//		}
//		envir.adapter.exec(r);
//	}

	@Override
	public void onPathTilesChanged() {
		envir.adapter.assertUIThread();
		if (pathTiles != null) {
			pathTiles.setInvalidAll();
			repaint();
		}
	}

	public double pt2m(int pt) {
		double m1deg = 111000;
		double lat1 = Utils.y2lat(centerXY.y - pt / 2, zoom);
		double lat2 = Utils.y2lat(centerXY.y + pt / 2, zoom);
		return (lat1 - lat2) * m1deg;
	}

	public double m2pt(double m) {
		double m1deg = 111000;
		double lat1 = Utils.y2lat(centerXY.y - 10 / 2, zoom);
		double lat2 = Utils.y2lat(centerXY.y + 10 / 2, zoom);
		return m * 10 / ((lat1 - lat2) * m1deg);
	}

	@Override
	public void updateSel() {
		envir.adapter.assertUIThread();
		selectionThread.movedTo(centerXY, zoom);
	}

	class SelectionThread extends Thread {
		{
			setDaemon(true);
			setPriority(MIN_PRIORITY);
			start();
		}

		public volatile ISelectable sel;

		private PointInt xy;
		private int zoom;
		private volatile boolean cancel;

		@Override
		public void run() {
			loop: for (;;) {
				PointInt xy;
				int zoom;
				synchronized (this) {
					while (this.xy == null) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					xy = this.xy;
					zoom = this.zoom;
					this.xy = null;
				}

				ISelectable sel = null;

				RectX screenRect;
				int x0;
				int x1;
				int y0;
				int y1;
				synchronized (lastDraw) {
					x0 = xy.x - lastDraw.w / 2;
					x1 = xy.x + lastDraw.w / 2;
					y0 = xy.y - lastDraw.h / 2;
					y1 = xy.y + lastDraw.h / 2;
				}
				double lon0 = Utils.x2lon(x0, zoom);
				double lonw = Utils.x2lon(x1, zoom) - lon0;
				double lat0 = Utils.y2lat(y1, zoom);
				double lath = Utils.y2lat(y0, zoom) - lat0;
				screenRect = new RectX(lon0, lat0, lonw, lath);

				Map<LocationX, Path> pms = new HashMap<LocationX, Path>();
				for (LocationX pm : envir.placemarks.getPlaceMarks()) {
					if (cancel)
						continue loop;
					pms.put(pm, null);
				}
				for (Path path : envir.paths.getPaths()) {
					if (!path.filter(screenRect))
						continue;
					if (!path.isEnabled())
						continue;
					for (LocationX pm : path.getPlaceMarks(zoom)) {
						if (cancel)
							continue loop;
						pms.put(pm, path);
					}
				}
				// System.out.println("sz= " + pms.size());
				LocationX pm = getNearest(pms.keySet(), xy, zoom, 10);
				if (pm != null) {
					Path path = pms.get(pm);
					if (path == null)
						sel = pm;
					else
						sel = new PathSelection(path, pm);
				} else {
					sel = null;
				}

				synchronized (this) {
					if (cancel)
						continue loop;

					if (sel != null && !sel.equals(this.sel) || sel == null
							&& this.sel != null) {
						this.sel = sel;
						envir.adapter.exec(new Runnable() {
							@Override
							public void run() {
								onPathTilesChanged();
							}
						});
					}
				}
			}
		}

		private LocationX getNearest(Collection<LocationX> placemarks,
				PointInt xy, int zoom, long maxDist) {
			maxDist = maxDist * maxDist;
			long dist = -1;
			LocationX pmNearest = null;

			for (LocationX pm : placemarks) {
				if (cancel)
					return null;
				long dx = pm.getX(zoom) - xy.x;
				long dy = pm.getY(zoom) - xy.y;
				long d = dx * dx + dy * dy;
				if ((maxDist == 0 || d < maxDist)
						&& (pmNearest == null || d < dist)) {
					pmNearest = pm;
					dist = d;
				}
			}

			return pmNearest;
		}

		public synchronized void cancel() {
			if (sel != null) {
				sel = null;
				envir.adapter.exec(new Runnable() {
					@Override
					public void run() {
						onPathTilesChanged();
					}
				});
			}
			cancel = true;
		}

		public synchronized void movedTo(PointInt xy, int zoom) {
			this.xy = xy;
			this.zoom = zoom;
			cancel = false;
			notify();
		}
	}

	private final SelectionThread selectionThread = new SelectionThread();

	public ISelectable getSelAsync() {
		return selectionThread.sel;
	}

	public void incInfoLevel() {
		envir.adapter.assertUIThread();
		if (infoLevel.ordinal() < InfoLevel.values().length - 1) {
			infoLevel = InfoLevel.values()[infoLevel.ordinal() + 1];
			onPathTilesChanged();
		}
	}

	public void decInfoLevel() {
		envir.adapter.assertUIThread();
		if (infoLevel.ordinal() > 0) {
			infoLevel = InfoLevel.values()[infoLevel.ordinal() - 1];
			onPathTilesChanged();
		}
	}

	public void clearPathTiles() {
		envir.adapter.assertUIThread();
		onPathTilesChanged();
	}

	public InfoLevel getInfoLevel() {
		return infoLevel;
	}

	public void setTarget() {
		ISelectable sel = selectionThread.sel;
		if (sel instanceof LocationX) {
			envir.placemarks.setTarget((LocationX) sel);
			clearPathTiles();
		}
	}

	public LocationX getTarget() {
		return envir.placemarks.getTarget();
	}

	@Override
	public void exec(Runnable r) {
		envir.adapter.exec(r);
	}

}
