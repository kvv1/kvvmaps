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

	private final IPlatformView platformView;

	private final MapViewParams mapPos = new MapViewParams();

	private InfoLevel infoLevel = InfoLevel.HIGH;

	private final MapTiles mapTiles;
	private final PathTiles pathTiles;

	private final SelectionThread selectionThread = new SelectionThread();
	private final LongSet tilesDrawn = new LongSet();

	private LocationX myLocation;

	private boolean myLocationDimmed;

	private final Environment envir;

	private final Diagram diagram;

	public CommonView(IPlatformView platformView, final Environment envir) {
		this.envir = envir;
		this.platformView = platformView;

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

	public void setMyLocation(LocationX locationX, boolean scroll) {

		LocationX oldLocation = myLocation;

		myLocation = locationX;
		myLocationDimmed = false;

		if (onScreen(oldLocation)) {
			int dx = (int) (mapPos.lon2scrX(myLocation.getLongitude()) - mapPos
					.lon2scrX(oldLocation.getLongitude()));
			int dy = (int) (mapPos.lat2scrY(myLocation.getLatitude()) - mapPos
					.lat2scrY(oldLocation.getLatitude()));
			animateBy(new PointInt(dx, dy));
			repaint();
		} else if (scroll) {
			animateTo(myLocation);
			repaint();
		}

	}

	private boolean onScreen(LocationX loc) {
		return loc != null
				&& (Math.abs(mapPos.lon2scrX(loc.getLongitude())) < platformView
						.getWidth() / 2 && Math.abs(mapPos.lat2scrY(loc
						.getLatitude())) < platformView.getHeight() / 2);

	}

	public void dimmMyLocation() {
		myLocationDimmed = true;
	}

	public boolean isOnMyLocation() {
		return myLocation != null
				&& Math.abs(mapPos.lon2scrX(myLocation.getLongitude())) < 2
				&& Math.abs(mapPos.lat2scrY(myLocation.getLatitude())) < 2;
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

		PointInt centerXY = new PointInt((int) mapPos.centerX(),
				(int) mapPos.centerY());

		int nxC = centerXY.x / Adapter.TILE_SIZE;
		int nyC = centerXY.y / Adapter.TILE_SIZE;
		long id = TileId.make(nxC, nyC, getZoom());
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
		if (tile == null || tile.content.maps.size() == 0)
			return null;
		return tile.content.maps.getFirst();
	}

	public void setTopMap(String map) {
		mapTiles.setTopMap(map);
	}

	public void zoomOut() {
		if (getZoom() > Utils.MIN_ZOOM)
			setZoom(getZoom() - 1);
	}

	public void zoomIn() {
		if (getZoom() < Utils.MAX_ZOOM)
			setZoom(getZoom() + 1);
	}

	public int getZoom() {
		return mapPos.getZoom();
	}

	public void setZoom(int zoom) {
		envir.adapter.assertUIThread();
		mapTiles.stopLoading();
		pathTiles.stopLoading();
		mapPos.setZoom(zoom);
		repaint();
		updateSel();
	}

	public void animateTo(LocationX loc, int dx, int dy) {
		envir.adapter.assertUIThread();
		mapPos.animateTo(loc.getLongitude(), loc.getLatitude(), dx, dy);
		repaint();
		updateSel();
	}

	public void animateTo(LocationX loc) {
		animateTo(loc, 0, 0);
	}

	private void animateBy(PointInt offset) {
		envir.adapter.assertUIThread();
		mapPos.animateBy(offset.x, offset.y);
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
		int locationH = ViewHelper.drawMyLocation(gc, mapPos, myLocation,
				isMyLocationDimmed());
		ViewHelper.drawCross(gc);
		ViewHelper.drawScale(gc, mapPos);
		// long time2 = System.currentTimeMillis();
		// System.out.println("t2 = " + (time2 - time1));

		drawDiagram(gc, locationH);

		if (p1 == null) {
			LocationX myLoc = myLocation;
			if (!isMyLocationDimmed())
				myLoc = null;
			ViewHelper
					.drawTarget(gc, mapPos, getLocation(), myLoc, getTarget());
		}
	}

	// public int x_scr2tiles(int x, int w) {
	// x -= w / 2;
	// return x + centerXY.x;
	// }
	//
	// public int y_scr2tiles(int y, int h) {
	// y -= h / 2;
	// return y + centerXY.y;
	// }
	//
	// public int x_tiles2scr(int x, int w) {
	// x -= centerXY.x;
	// return x + w / 2;
	// }
	//
	// public int y_tiles2scr(int y, int h) {
	// y -= centerXY.y;
	// return y + h / 2;
	// }

	private void drawTiles(GC gc) {
		tilesDrawn.clear();

		PointInt centerXY = new PointInt((int) mapPos.centerX(),
				(int) mapPos.centerY());

		int w = gc.getWidth();
		int h = gc.getHeight();

		int nx0 = (int) mapPos.scr2geoX(-w / 2) / Adapter.TILE_SIZE;
		int x0 = (int) mapPos.geo2scrX(nx0 * Adapter.TILE_SIZE) + w / 2;
		// int x0 = x_tiles2scr(nx0 * Adapter.TILE_SIZE, w);
		int ny0 = (int) mapPos.scr2geoY(-h / 2) / Adapter.TILE_SIZE;
		int y0 = (int) mapPos.geo2scrY(ny0 * Adapter.TILE_SIZE) + h / 2;
		// int y0 = y_tiles2scr(ny0 * Adapter.TILE_SIZE, h);

		int x = x0;
		for (int nx = nx0; x < w; nx++, x += Adapter.TILE_SIZE) {
			int y = y0;
			for (int ny = ny0; y < h; ny++, y += Adapter.TILE_SIZE) {
				long id = TileId.make(nx, ny, getZoom());

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
		platformView.repaint();
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

	public LocationX getLocation() {
		return getLocation(0, 0);
	}

	public LocationX getLocation(int dx, int dy) {
		envir.adapter.assertUIThread();
		return new LocationX(mapPos.scrX2lon(dx), mapPos.scrY2lat(dy));
	}

	private void cancelSel() {
		envir.adapter.assertUIThread();
		selectionThread.cancel();
	}

	private void updateSel() {
		envir.adapter.assertUIThread();
		selectionThread.set((int) mapPos.centerX(), (int) mapPos.centerY(),
				platformView.getWidth(), platformView.getHeight(), getZoom(),
				envir.adapter, envir.placemarks, envir.paths,
				new SelectionThread.Callback() {
					@Override
					public void selectionChanged(ISelectable sel) {
						CommonView.this.sel = sel;
						CommonView.this.invalidatePathTiles();
						if (sel instanceof PathSelection) {
							PathSelection sel1 = (PathSelection) sel;
							diagram.set(sel1.path, sel1.pm,
									platformView.getWidth(),
									platformView.getHeight());
						} else {
							diagram.set(null, null, platformView.getWidth(),
									platformView.getHeight());
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

	public void fixMap(String map) {
		mapTiles.fixMap(map);
		repaint();
	}

	public String fixMap(boolean fix) {
		if (!fix) {
			mapTiles.fixMap(null);
			repaint();
			return null;
		} else {
			String topMap = getTopMap();
			mapTiles.fixMap(topMap);
			repaint();
			return topMap;
		}
	}
}
