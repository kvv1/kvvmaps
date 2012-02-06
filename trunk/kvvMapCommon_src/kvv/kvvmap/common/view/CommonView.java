package kvv.kvvmap.common.view;

import java.util.Collections;
import java.util.List;

import android.os.Debug;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.LongSet;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maps.Maps.MapsListener;
import kvv.kvvmap.common.pacemark.IPlaceMarksListener;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.PathDrawer;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileContent;
import kvv.kvvmap.common.tiles.TileId;
import kvv.kvvmap.common.tiles.TileLoader.TileSource;
import kvv.kvvmap.common.tiles.Tiles;

public class CommonView implements ICommonView {

	private volatile ISelectable sel;

	private final IPlatformView platformView;

	private final MapViewParams mapPos = new MapViewParams();

	private volatile InfoLevel infoLevel = InfoLevel.HIGH;

	private final Tiles tiles;

	private final SelectionThread selectionThread = new SelectionThread();
	private final LongSet tilesDrawn = new LongSet();

	private LocationX myLocation;

	private boolean myLocationDimmed;

	private final Environment envir;

	private boolean scrolling;

	// private PointInt sreenCenter = new PointInt(0, 0);
	private RotationMode rotationMode = RotationMode.ROTATION_NONE;

	public enum RotationMode {
		ROTATION_NONE, ROTATION_COMPASS, ROTATION_GPS
	}

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
				if (tiles != null) {
					tiles.setInvalid(id);
					updateSel();
					if (tilesDrawn.contains(id)) {
						repaint();
					}
				}
			}

			private Runnable r = new Runnable() {
				@Override
				public void run() {
					onPathTilesChanged();
				}
			};

			@Override
			public void onPathTilesChangedAsync() {
				envir.adapter.execUI(r);
			}
		};

		envir.placemarks.setDoc(pmListener);
		envir.paths.setDoc(pmListener);
		envir.maps.setListener(new MapsListener() {
			@Override
			public void mapAdded(String name) {
				if (tiles != null) {
					tiles.cancelLoading();
					tiles.setInvalidAll();
					Adapter.log("mapAdded notified");
					repaint();
				}
			}
		});

		this.tiles = new Tiles(envir.adapter, new TileSource() {
			@Override
			public Tile loadAsync(long id) {
				TileContent content = new TileContent();

				// long t = System.currentTimeMillis();

				Img img = envir.maps.load(TileId.nx(id), TileId.ny(id),
						TileId.zoom(id), content);
				if (img == null)
					return null;

				// long t1 = System.currentTimeMillis();
				// t = t1 - t;

				GC gc = envir.adapter.getGC(img.img);

				InfoLevel infoLevel = CommonView.this.infoLevel;
				if (infoLevel.ordinal() > 0) {
					gc.setAntiAlias(true);
					ISelectable sel1 = sel;
					PathDrawer.drawPaths(envir.paths, gc, id, infoLevel, sel1);
					PathDrawer.drawPlacemarks(envir.placemarks, gc, id,
							infoLevel, sel1);
				}

				// t1 = System.currentTimeMillis() - t1;
				// Adapter.log("load tile " + t + " " + t1);

				return new Tile(envir.adapter, id, img, content);
			}
		}, Adapter.MAP_TILES_CACHE_SIZE) {
			@Override
			protected void loaded(Tile tile) {
				repaint();
			}
		};
	}

	public RotationMode getRotationMode() {
		return rotationMode;
	}

	public void setRotationMode(RotationMode rotationMode) {
		this.rotationMode = rotationMode;
		setAngle(0);
		if (rotationMode == RotationMode.ROTATION_GPS)
			scrollToRotationGPS();
	}

	public LocationX getMyLocation() {
		return myLocation;
	}

	public boolean isMyLocationDimmed() {
		return myLocationDimmed;
	}

	private void scrollToRotationGPS() {
		if (myLocation == null)
			return;

		setAngle(-myLocation.getBearing());

		double x = mapPos.loc2scrX(myLocation);
		double y = mapPos.loc2scrY(myLocation);

		y -= platformView.getHeight() / 4;

		double lon = mapPos.scr2lon(x, y);
		double lat = mapPos.scr2lat(x, y);

		animateTo(lon, lat);
	}

	public void setMyLocation(LocationX loc, boolean scroll) {

		LocationX oldLocation = myLocation;

		myLocation = loc;
		myLocationDimmed = false;

		if (rotationMode == RotationMode.ROTATION_GPS) {
			scrollToRotationGPS();
		} else {
			if (onScreen(oldLocation)) {
				double dx = mapPos.loc2scrX(myLocation)
						- mapPos.loc2scrX(oldLocation);
				double dy = mapPos.loc2scrY(myLocation)
						- mapPos.loc2scrY(oldLocation);
				scrollBy(dx, dy);
			} else if (scroll) {
				animateTo(myLocation);
			}
		}

	}

	private boolean onScreen(LocationX loc) {
		return loc != null
				&& (Math.abs(mapPos.loc2scrX(loc)) < platformView.getWidth() / 2 && Math
						.abs(mapPos.loc2scrY(loc)) < platformView.getHeight() / 2);
	}

	public void dimmMyLocation() {
		myLocationDimmed = true;
	}

	public boolean isOnMyLocation() {
		return myLocation != null && Math.abs(mapPos.loc2scrX(myLocation)) < 2
				&& Math.abs(mapPos.loc2scrY(myLocation)) < 2;
	}

	public void startScrolling() {
		scrolling = true;
		cancelSel();
	}

	public void endScrolling() {
		scrolling = false;
		updateSel();
		repaint();
		System.gc();
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
		Tile tile = tiles.getTile(id, centerXY, false);
		return tile;
	}

	public void reorderMaps() {
		Tile tile = getCenterTile();
		if (tile != null && tile.isMultiple()) {
			envir.maps.reorder(tile.content.maps.getLast());
			tiles.setInvalidAll();
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
		envir.maps.setTopMap(map);
		tiles.setInvalidAll();
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

	// private static float normalize(float deg) {
	// while (deg < -180)
	// deg += 360;
	// while (deg > 180)
	// deg -= 360;
	// return deg;
	// }

	public void setAngle(float deg) {
		envir.adapter.assertUIThread();

		// deg = normalize(deg);
		//
		// float oldDeg = normalize(mapPos.angle());
		//
		// if (deg - oldDeg < 180) {
		//
		// }
		//
		// envir.adapter.execUI(runnable, 100);

		mapPos.setAngle(deg);
		repaint();
	}

	public void setZoom(int zoom) {
		envir.adapter.assertUIThread();
		tiles.cancelLoading();
		mapPos.setZoom(zoom);
		if (rotationMode == RotationMode.ROTATION_GPS)
			scrollToRotationGPS();
		repaint();
		updateSel();
	}

	public void animateTo(double lon, double lat) {
		envir.adapter.assertUIThread();
		mapPos.animateTo(lon, lat);
		repaint();
		updateSel();
	}

	public void animateTo(LocationX loc) {
		envir.adapter.assertUIThread();
		mapPos.animateTo(loc.getLongitude(), loc.getLatitude());
		repaint();
		updateSel();
	}

	public void scrollBy(double dx, double dy) {
		envir.adapter.assertUIThread();
		mapPos.scrollBy(dx, dy);
		repaint();
		cancelSel();
	}

	public void draw(GC gc) {
		// Adapter.log("draw");

		gc.setAntiAlias(true);
		// long time = System.currentTimeMillis();
		int w = gc.getWidth();
		int h = gc.getHeight();
		gc.setTransform((float) (mapPos.angle()), w / 2, h / 2);
		try {
			drawTiles(gc);
		} catch (Exception e) {
		}
		gc.clearTransform();
		// long time1 = System.currentTimeMillis();
		// System.out.println("t1 = " + (time1 - time));
		ViewHelper
				.drawMyLocationArrow(gc, mapPos, myLocation, myLocationDimmed);

		ViewHelper.drawCross(gc);
		ViewHelper.drawScale(gc, mapPos);
		// long time2 = System.currentTimeMillis();
		// System.out.println("t2 = " + (time2 - time1));

		LocationX targ = getTarget();

		if (targ != null) {
			ViewHelper.drawLine(gc, mapPos, getLocation(), targ,
					COLOR.dimm(COLOR.TARG_COLOR));
			if (myLocation != null)
				ViewHelper.drawLine(gc, mapPos, myLocation, targ,
						COLOR.dimm(COLOR.ARROW_COLOR));
		}

		if (Adapter.debugDraw) {
			int usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
			int freeMegs = (int) (Debug.getNativeHeapFreeSize() / 1048576L);
			int allMegs = (int) (Debug.getNativeHeapSize() / 1048576L);
			String mem = "J " + Runtime.getRuntime().totalMemory() / 1024
					/ 1024 + " " + Runtime.getRuntime().freeMemory() / 1024
					/ 1024 + " " + Runtime.getRuntime().maxMemory() / 1024
					/ 1024 + " " + " N " + usedMegs + " " + freeMegs + " "
					+ allMegs + " ";
			gc.setTextSize(20);

			ViewHelper.drawText(gc, mem, 10, 50, COLOR.RED, COLOR.WHITE);
			//
			// gc.setColor(COLOR.RED);
			// gc.drawText(mem, 10, 50);
		}

	}

	private void drawTiles(GC gc) {
		tilesDrawn.clear();

		int w = gc.getWidth();
		int h = gc.getHeight();

		RectInt screenRect = getScreenRectInt();
		int nx0 = screenRect.getX() / Adapter.TILE_SIZE;
		int ny0 = screenRect.getY() / Adapter.TILE_SIZE;
		int nx1 = (screenRect.getX() + screenRect.getW()) / Adapter.TILE_SIZE;
		int ny1 = (screenRect.getY() + screenRect.getH()) / Adapter.TILE_SIZE;

		int x0 = (int) (nx0 * Adapter.TILE_SIZE - mapPos.centerX() + w / 2);
		int y0 = (int) (ny0 * Adapter.TILE_SIZE - mapPos.centerY() + h / 2);

		PointInt centerXY = new PointInt((int) mapPos.centerX(),
				(int) mapPos.centerY());

		gc.setAntiAlias(true);

		int x = x0;
		for (int nx = nx0; nx <= nx1; nx++, x += Adapter.TILE_SIZE) {
			int y = y0;
			for (int ny = ny0; ny <= ny1; ny++, y += Adapter.TILE_SIZE) {
				long id = TileId.make(nx, ny, getZoom());
				tiles.drawTile(gc, centerXY, id, x, y,
						platformView.loadDuringScrolling() || !scrolling,
						mapPos.getZoom(), mapPos.getPrevZoom());
				tilesDrawn.add(id);
			}
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
		} else {
			infoLevel = InfoLevel.values()[0];
		}
		invalidatePathTiles();
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
		if (tiles != null) {
			tiles.cancelLoading();
			tiles.setInvalidAll();
			repaint();
		}
	}

	public LocationX getTarget() {
		return envir.placemarks.getTarget();
	}

	public LocationX getLocation() {
		envir.adapter.assertUIThread();
		return mapPos.getLocation();
	}

	public LocationX getLocation(int dx, int dy) {
		envir.adapter.assertUIThread();
		return new LocationX(mapPos.scr2lon(dx, dy), mapPos.scr2lat(dx, dy));
	}

	private void cancelSel() {
		envir.adapter.assertUIThread();
		selectionThread.cancel();
	}

	private void select(ISelectable sel) {
		CommonView.this.sel = sel;
		CommonView.this.invalidatePathTiles();
		if (sel instanceof PathSelection) {
			platformView.pathSelected((PathSelection) sel);
		} else {
			platformView.pathSelected(null);
		}
	}

	private SelectionThread.Callback selCallback = new SelectionThread.Callback() {
		@Override
		public void selectionChanged(ISelectable sel) {
			select(sel);
		}
	};

	private void updateSel() {
		envir.adapter.assertUIThread();
		if (rotationMode == RotationMode.ROTATION_GPS) {
			select(null);
			return;
		}
		selectionThread.set((int) mapPos.centerX(), (int) mapPos.centerY(),
				getScreenRect(), getZoom(), envir.adapter, envir.placemarks,
				envir.paths, selCallback);
	}

	private RectInt getScreenRectInt() {
		int w = platformView.getWidth();
		int h = platformView.getHeight();

		int x1 = (int) mapPos.scr2geoX(-w / 2, -h / 2);
		int x2 = (int) mapPos.scr2geoX(w / 2, h / 2);
		int x3 = (int) mapPos.scr2geoX(w / 2, -h / 2);
		int x4 = (int) mapPos.scr2geoX(-w / 2, h / 2);
		int y1 = (int) mapPos.scr2geoY(-w / 2, -h / 2);
		int y2 = (int) mapPos.scr2geoY(w / 2, h / 2);
		int y3 = (int) mapPos.scr2geoY(-w / 2, h / 2);
		int y4 = (int) mapPos.scr2geoY(w / 2, -h / 2);

		int minX = Math.min(Math.min(x1, x2), Math.min(x3, x4));
		int maxX = Math.max(Math.max(x1, x2), Math.max(x3, x4));
		int minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
		int maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));

		RectInt rect = new RectInt();
		rect.set(minX, minY, maxX - minX, maxY - minY);
		return rect;
	}

	private RectX getScreenRect() {
		int w = platformView.getWidth();
		int h = platformView.getHeight();

		double lon1 = mapPos.scr2lon(-w / 2, -h / 2);
		double lon2 = mapPos.scr2lon(w / 2, h / 2);
		double lon3 = mapPos.scr2lon(-w / 2, h / 2);
		double lon4 = mapPos.scr2lon(w / 2, -h / 2);
		double lat1 = mapPos.scr2lat(-w / 2, -h / 2);
		double lat2 = mapPos.scr2lat(w / 2, h / 2);
		double lat3 = mapPos.scr2lat(-w / 2, h / 2);
		double lat4 = mapPos.scr2lat(w / 2, -h / 2);

		double minLon = Math.min(Math.min(lon1, lon2), Math.min(lon3, lon4));
		double maxLon = Math.max(Math.max(lon1, lon2), Math.max(lon3, lon4));
		double minLat = Math.min(Math.min(lat1, lat2), Math.min(lat3, lat4));
		double maxLat = Math.max(Math.max(lat1, lat2), Math.max(lat3, lat4));

		return new RectX(minLon, minLat, maxLon - minLon, maxLat - minLat);
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

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~CommonView");
		super.finalize();
	}

	public void fixMap(String map) {
		envir.maps.fixMap(map);
		tiles.setInvalidAll();
		repaint();
	}

	public String fixMap(boolean fix) {
		if (!fix) {
			envir.maps.fixMap(null);
			tiles.setInvalidAll();
			repaint();
			return null;
		} else {
			String topMap = getTopMap();
			envir.maps.fixMap(topMap);
			tiles.setInvalidAll();
			repaint();
			return topMap;
		}
	}

}
