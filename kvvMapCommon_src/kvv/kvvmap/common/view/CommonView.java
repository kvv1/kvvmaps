package kvv.kvvmap.common.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.LongSet;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maptiles.MapTiles;
import kvv.kvvmap.common.pacemark.IPlaceMarksListener;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.pathtiles.PathTiles;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileId;

public class CommonView implements ICommonView {

	private volatile ISelectable sel;

	private final IPlatformView platformViewView;

	private int zoom = Utils.MIN_ZOOM;

	private PointInt centerXY;

	private InfoLevel infoLevel = InfoLevel.HIGH;

	private final MapTiles mapTiles;
	private final PathTiles pathTiles;

	private final SelectionThread selectionThread = new SelectionThread();
	private final LongSet tilesDrawn = new LongSet();

	private LocationX myLocation;

	private boolean myLocationDimmed;

	private final Environment envir;

	private final Diagram diagram;

	public CommonView(IPlatformView platformViewView, final Environment envir) {
		this.envir = envir;
		this.platformViewView = platformViewView;

		IPlaceMarksListener pmListener = new IPlaceMarksListener() {

			@Override
			public void onPathTilesChanged() {
				envir.adapter.assertUIThread();
				CommonView.this.invalidatePathTiles();
				updateSel();
			}

			@Override
			public void onPathTileChanged(long id) {
				envir.adapter.assertUIThread();
				if (pathTiles != null) {
					pathTiles.setInvalid(id);
					updateSel();
					if (tilesDrawn.contains(id)) {
						repaint();
					}
				}
			}

			@Override
			public void onPathTilesChangedAsync() {
				envir.adapter.execUI(new Runnable() {
					@Override
					public void run() {
						onPathTilesChanged();
					}
				});
			}
		};

		envir.placemarks.setDoc(pmListener);
		envir.paths.setDoc(pmListener);

		this.mapTiles = new MapTiles(envir.adapter, envir.maps,
				Adapter.MAP_TILES_CACHE_SIZE) {
			@Override
			protected void loaded(Tile tile) {
				repaint();
			}
		};
		this.pathTiles = new PathTiles(envir, Adapter.PATH_TILES_CACHE_SIZE) {

			@Override
			protected void loaded(Tile tile) {
				repaint();
			}

			@Override
			protected InfoLevel getInfoLevel() {
				return infoLevel;
			}

			@Override
			protected ISelectable getSelAsync() {
				return sel;
			}
		};

		diagram = new Diagram(envir.adapter, this);
	}

	public LocationX getMyLocation() {
		return myLocation;
	}

	public boolean isMyLocationDimmed() {
		return myLocationDimmed;
	}

	public void setMyLocation(LocationX locationX, boolean forceScroll) {

		LocationX oldLocation = myLocation;
		boolean wasDimmed = myLocationDimmed;

		myLocation = locationX;
		myLocationDimmed = false;

		forceScroll |= wasDimmed;

		forceScroll |= oldLocation != null
				&& (Math.abs(oldLocation.getX(zoom) - centerXY.x) < platformViewView
						.getWidth() / 2 && Math.abs(oldLocation.getY(zoom)
						- centerXY.y) < platformViewView.getHeight() / 2);

		if (forceScroll) {
			if (oldLocation != null && !wasDimmed) {
				int dx = myLocation.getX(zoom) - oldLocation.getX(zoom);
				int dy = myLocation.getY(zoom) - oldLocation.getY(zoom);
				animateBy(new PointInt(dx, dy));
			} else {
				animateTo(myLocation);
			}
		}

		repaint();
	}

	public void dimmMyLocation() {
		myLocationDimmed = true;
	}

	public boolean isOnMyLocation() {
		return myLocation != null && myLocation.getX(zoom) == centerXY.x
				&& myLocation.getY(zoom) == centerXY.y;
	}

	private PointInt p1;

	private KineticScrollingFilter ksf = new KineticScrollingFilterImpl1() {

		@Override
		public void onMousePressed(int x, int y) {
			p1 = new PointInt(x, y);
		}

		@Override
		public void onMouseReleased(int x, int y) {
			p1 = null;
			updateSel();
			repaint();
		}

		@Override
		public void onMouseDragged(int x, int y) {
			if (p1 != null) {
				PointInt offset = new PointInt(p1.x - x, p1.y - y);
				animateBy(offset);
				p1 = new PointInt(x, y);
			}
		}

		@Override
		public void exec(Runnable r) {
			envir.adapter.execUI(r);
		}

	};

	public void onMove(int x, int y) {
		ksf.mouseDragged(x, y);
	}

	public void onDown(int x, int y) {
		ksf.mousePressed(x, y);
	}

	public void onUp(int x, int y) {
		ksf.mouseReleased(x, y);
	}

	public boolean isMultiple() {
		Tile tile = getCenterTile();
		if (tile == null)
			return false;
		return tile.isMultiple();
	}

	public List<String> getCenterMaps() {
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
		Tile tile = mapTiles.getTile(id, centerXY, false);
		return tile;
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
		return tile.content.maps.getFirst();
	}

	public void setTopMap(String map) {
		mapTiles.setTopMap(map);
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
		mapTiles.stopLoading();
		pathTiles.stopLoading();
		animateTo(loc);
	}

	public void animateTo(LocationX loc, int dx, int dy) {
		envir.adapter.assertUIThread();
		if (loc.getLatitude() > 85 || loc.getLatitude() < -85)
			return;
		int x = (int) (Utils.lon2x(loc.getLongitude(), zoom));
		int y = (int) (Utils.lat2y(loc.getLatitude(), zoom));
		centerXY = new PointInt(x - dx, y - dy);
		repaint();
		updateSel();
	}

	public void animateTo(LocationX loc) {
		animateTo(loc, 0, 0);
	}

	private void animateBy(PointInt offset) {
		envir.adapter.assertUIThread();
		int x = centerXY.x + offset.x;
		int y = centerXY.y + offset.y;
		double lat = Utils.y2lat(y, zoom);
		if (lat > 85 || lat < -85)
			return;
		centerXY = new PointInt(x, y);
		repaint();
		cancelSel();
	}

	public void draw(GC gc) {
		// Adapter.log("draw");

		gc.setAntiAlias(true);
		// long time = System.currentTimeMillis();
		drawTiles(gc);
		// long time1 = System.currentTimeMillis();
		// System.out.println("t1 = " + (time1 - time));
		int locationH = ViewHelper.drawMyLocation(gc, this, myLocation,
				isMyLocationDimmed());
		ViewHelper.drawCross(gc);
		ViewHelper.drawScale(gc, this);
		// long time2 = System.currentTimeMillis();
		// System.out.println("t2 = " + (time2 - time1));

		drawDiagram(gc, locationH);

		if (p1 == null) {
			LocationX myLoc = myLocation;
			if (!isMyLocationDimmed())
				myLoc = null;
			ViewHelper.drawTarget(gc, this, myLoc);
		}
	}

	private void drawTiles(GC gc) {
		int w = gc.getWidth();
		int h = gc.getHeight();

		tilesDrawn.clear();

		int x0 = centerXY.x - w / 2;
		int y0 = centerXY.y - h / 2;

		int nx0 = x0 / Adapter.TILE_SIZE;
		int ny0 = y0 / Adapter.TILE_SIZE;

		int nx1 = (x0 + w - 1) / Adapter.TILE_SIZE;
		int ny1 = (y0 + h - 1) / Adapter.TILE_SIZE;

		for (int nx = nx0; nx <= nx1; nx++) {
			for (int ny = ny0; ny <= ny1; ny++) {
				long id = TileId.get(nx, ny, zoom);

				int x = (nx * Adapter.TILE_SIZE) - x0;
				int y = (ny * Adapter.TILE_SIZE) - y0;

				Tile tile = mapTiles.getTile(id, centerXY, p1 == null);
				if (tile != null)
					tile.draw(gc, x, y);

				if (getInfoLevel().ordinal() > 0) {
					tile = pathTiles.getTile(id, centerXY, p1 == null);
					if (tile != null)
						tile.draw(gc, x, y);
				}

				tilesDrawn.add(id);
			}
		}
	}

	public void saveState() {
		Properties props = new Properties();

		String sLon = Double.toString(getLocation().getLongitude());
		String sLat = Double.toString(getLocation().getLatitude());
		String sZoom = Integer.toString(getZoom());

		props.put("lon", sLon);
		props.put("lat", sLat);
		props.put("zoom", sZoom);
		try {
			props.save(new FileOutputStream("a.properties"), "");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void loadState() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("a.properties"));
		} catch (IOException e1) {
		}

		String sLon = props.getProperty("lon");
		String sLat = props.getProperty("lat");
		String sZoom = props.getProperty("zoom");

		if (sLon != null && sLat != null && sZoom != null) {
			double lon = Double.parseDouble(sLon);
			double lat = Double.parseDouble(sLat);
			int zoom = Integer.parseInt(sZoom);
			setZoom(zoom);
			animateTo(new LocationX(lon, lat));
		}

	}

	@Override
	public void repaint() {
		platformViewView.repaint();
	}

	public ISelectable getSel() {
		return sel;
	}

	public void dispose() {
	}

	public void incInfoLevel() {
		envir.adapter.assertUIThread();
		if (infoLevel.ordinal() < InfoLevel.values().length - 1) {
			infoLevel = InfoLevel.values()[infoLevel.ordinal() + 1];
			invalidatePathTiles();
		}
	}

	public void decInfoLevel() {
		envir.adapter.assertUIThread();
		if (infoLevel.ordinal() > 0) {
			infoLevel = InfoLevel.values()[infoLevel.ordinal() - 1];
			invalidatePathTiles();
		}
	}

	public InfoLevel getInfoLevel() {
		return infoLevel;
	}

	public void invalidatePathTiles() {
		envir.adapter.assertUIThread();
		if (pathTiles != null) {
			pathTiles.setInvalidAll();
			repaint();
		}
	}

	public LocationX getTarget() {
		return envir.placemarks.getTarget();
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

	private void cancelSel() {
		envir.adapter.assertUIThread();
		selectionThread.cancel();
	}

	private void updateSel() {
		envir.adapter.assertUIThread();
		selectionThread.set(centerXY.x, centerXY.y,
				platformViewView.getWidth(), platformViewView.getHeight(),
				zoom, envir.adapter, envir.placemarks, envir.paths,
				new SelectionThread.Callback() {
					@Override
					public void selectionChanged(ISelectable sel) {
						CommonView.this.sel = sel;
						CommonView.this.invalidatePathTiles();
						if (sel instanceof PathSelection) {
							PathSelection sel1 = (PathSelection) sel;
							diagram.set(sel1.path, sel1.pm,
									platformViewView.getWidth(),
									platformViewView.getHeight());
						} else {
							diagram.set(null, null,
									platformViewView.getWidth(),
									platformViewView.getHeight());
						}
					}
				});
	}

	public void animateToMyLocation() {
		LocationX myLoc = getMyLocation();
		if (myLoc != null)
			animateTo(myLoc);
	}

	public void animateToTarget() {
		LocationX target = getTarget();
		if (target != null)
			animateTo(target);
	}

	public void onSizeChanged(int w, int h) {
		updateSel();
		repaint();
	}

	private void drawDiagram(GC gc, int locationH) {
		if (getInfoLevel().ordinal() != 0)
			diagram.draw(gc, locationH);
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~CommonView");
		super.finalize();
	}

}
