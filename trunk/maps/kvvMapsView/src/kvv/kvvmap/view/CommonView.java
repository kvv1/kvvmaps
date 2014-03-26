package kvv.kvvmap.view;

import java.util.Collections;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.RectInt;
import kvv.kvvmap.adapter.RectX;
import kvv.kvvmap.maps.Maps.MapsListener;
import kvv.kvvmap.placemark.IPlaceMarksListener;
import kvv.kvvmap.placemark.PathDrawer;
import kvv.kvvmap.placemark.PathSelection;
import kvv.kvvmap.tiles.Tile;
import kvv.kvvmap.tiles.TileContent;
import kvv.kvvmap.tiles.TileDrawer;
import kvv.kvvmap.tiles.TileLoader;
import kvv.kvvmap.tiles.TileLoader.Callback;
import kvv.kvvmap.tiles.TileLoader.TileSource;
import kvv.kvvmap.tiles.Tiles;
import kvv.kvvmap.util.COLOR;
import kvv.kvvmap.util.ISelectable;
import kvv.kvvmap.util.Img;
import kvv.kvvmap.util.InfoLevel;
import kvv.kvvmap.util.LongSet;
import kvv.kvvmap.util.TileId;
import kvv.kvvmap.util.Utils;

public class CommonView implements ICommonView {

	private volatile ISelectable sel;

	private final IPlatformView platformView;

	private final MapViewParams viewParams = new MapViewParams();

	private volatile InfoLevel infoLevel = InfoLevel.HIGH;

	private final Tiles tiles;

	private final SelectionThread selectionThread;
	private final LongSet tilesDrawn = new LongSet();

	private LocationX myLocation;

	private boolean myLocationDimmed;

	private final Environment envir;

	private boolean scrolling;

	private RotationMode rotationMode = RotationMode.ROTATION_NONE;

	private final TileDrawer tileDrawer;


	public enum RotationMode {
		ROTATION_NONE, ROTATION_COMPASS, ROTATION_GPS
	}

	private int getScreenCenterX() {
		return platformView.getWidth() / 2;
	}

	private int getScreenCenterY() {
		if (rotationMode == RotationMode.ROTATION_GPS)
			return platformView.getHeight() * 3 / 4;
		else
			return platformView.getHeight() / 2;
	}

	public CommonView(IPlatformView platformView, final Environment envir) {
		this.envir = envir;
		this.platformView = platformView;
		selectionThread = new SelectionThread(envir.adapter);

		IPlaceMarksListener pmListener = new IPlaceMarksListener() {

			@Override
			public void onPathTilesChanged() {
				invalidateTiles();
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

		envir.placemarks.setListener(pmListener);
		envir.paths.setListener(pmListener);
		envir.maps.setListener(new MapsListener() {
			@Override
			public void mapAdded(String name) {
				invalidateTiles();
			}
		});

		TileSource tileSource = new TileSource() {
			@Override
			public Tile loadAsync(long id) {
				TileContent content = new TileContent();

				// long t = System.currentTimeMillis();

				Img img = envir.maps.loadAsync(TileId.nx(id), TileId.ny(id),
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
		};

		TileLoader tileLoader = new TileLoader(envir.adapter, tileSource, new Callback() {
			@Override
			public void loaded(Tile tile) {
				tiles.putTile(tile);
			}
		});
		
		this.tiles = new Tiles(envir.adapter, tileLoader) {
			@Override
			protected void onTileLoaded(Tile tile) {
				repaint();
			}
		};
		
		this.tileDrawer = new TileDrawer(envir.adapter, this.tiles);
	}

	@Override
	public RotationMode getRotationMode() {
		return rotationMode;
	}

	@Override
	public void setRotationMode(RotationMode rotationMode) {
		this.rotationMode = rotationMode;
		setAngle(0);
		if (rotationMode == RotationMode.ROTATION_GPS)
			scrollToRotationGPS();
	}

	@Override
	public LocationX getMyLocation() {
		return myLocation;
	}

//	public boolean isMyLocationDimmed() {
//		return myLocationDimmed;
//	}

	private void scrollToRotationGPS() {
		if (myLocation == null)
			return;

		if (myLocation.hasBearing() && myLocation.getSpeed() * 3.6 > 5)
			setAngle(-myLocation.getBearing());

		animateTo(myLocation);
	}

	@Override
	public void setMyLocation(LocationX loc, boolean scroll) {

		LocationX oldLocation = myLocation;

		myLocation = loc;
		myLocationDimmed = false;

		if (rotationMode == RotationMode.ROTATION_GPS) {
			scrollToRotationGPS();
		} else {
			if (oldLocation != null
					&& getScreenRect(null).contains(oldLocation.getLongitude(),
							oldLocation.getLatitude())) {
				double dx = viewParams.loc2scrX(myLocation)
						- viewParams.loc2scrX(oldLocation);
				double dy = viewParams.loc2scrY(myLocation)
						- viewParams.loc2scrY(oldLocation);
				scrollBy(dx, dy);
			} else if (scroll) {
				animateTo(myLocation);
			}
		}

	}

	@Override
	public void dimmMyLocation() {
		myLocationDimmed = true;
	}

	@Override
	public boolean isOnMyLocation() {
		return myLocation != null
				&& Math.abs(viewParams.loc2scrX(myLocation)) < 2
				&& Math.abs(viewParams.loc2scrY(myLocation)) < 2;
	}

	@Override
	public void startScrolling() {
		scrolling = true;
		cancelSel();
	}

	@Override
	public void endScrolling() {
		scrolling = false;
		updateSel();
		repaint();
		System.gc();
	}

	@Override
	public boolean isMultiple() {
		Tile tile = getCenterTile();
		if (tile == null)
			return false;
		return tile.isMultiple();
	}

	@Override
	public List<String> getCenterMaps() {
		Tile tile = getCenterTile();
		if (tile == null)
			return Collections.emptyList();
		return tile.content.maps;
	}

	private Tile getCenterTile() {
		envir.adapter.assertUIThread();

		int centerX = (int) viewParams.centerX();
		int centerY = (int) viewParams.centerY();

		int nxC = centerX / Adapter.TILE_SIZE;
		int nyC = centerY / Adapter.TILE_SIZE;
		long id = TileId.make(nxC, nyC, getZoom());
		Tile tile = tiles.getTile(id, centerX, centerY, false);
		return tile;
	}

	@Override
	public void reorderMaps() {
		Tile tile = getCenterTile();
		if (tile != null && tile.isMultiple()) {
			envir.maps.reorder(tile.content.maps.getLast());
			invalidateTiles();
		}
	}

	@Override
	public String getTopMap() {
		envir.adapter.assertUIThread();
		Tile tile = getCenterTile();
		if (tile == null || tile.content.maps.size() == 0)
			return null;
		return tile.content.maps.getFirst();
	}

	@Override
	public void setTopMap(String map) {
		envir.maps.setTopMap(map);
		invalidateTiles();
	}

	@Override
	public void zoomOut() {
		if (getZoom() > Utils.MIN_ZOOM)
			setZoom(getZoom() - 1);
	}

	@Override
	public void zoomIn() {
		if (getZoom() < Utils.MAX_ZOOM)
			setZoom(getZoom() + 1);
	}

	@Override
	public int getZoom() {
		return viewParams.getZoom();
	}

	@Override
	public void setAngle(float deg) {
		envir.adapter.assertUIThread();
		viewParams.setAngle(deg);
		repaint();
	}

	@Override
	public void setZoom(int zoom) {
		envir.adapter.assertUIThread();
		tiles.cancelLoading();
		viewParams.setZoom(zoom);
		if (rotationMode == RotationMode.ROTATION_GPS)
			scrollToRotationGPS();
		repaint();
		updateSel();
	}

	private void animateTo(double lon, double lat) {
		envir.adapter.assertUIThread();
		viewParams.animateTo(lon, lat);
		repaint();
		updateSel();
	}

	@Override
	public void animateTo(LocationX loc) {
		animateTo(loc.getLongitude(), loc.getLatitude());
	}

	@Override
	public void scrollBy(double dx, double dy) {
		envir.adapter.assertUIThread();
		viewParams.scrollBy(dx, dy);
		repaint();
		cancelSel();
	}

	@Override
	public void draw(GC gc) {
		// Adapter.log("draw");

		int x0 = getScreenCenterX();
		int y0 = getScreenCenterY();

		gc.setAntiAlias(true);
		// long time = System.currentTimeMillis();
		// int w = gc.getWidth();
		// int h = gc.getHeight();
		gc.setTransform((float) (viewParams.angle()), getScreenCenterX(),
				getScreenCenterY());
		try {
			drawTiles(gc, x0, y0);
		} catch (Exception e) {
		}
		gc.clearTransform();
		// long time1 = System.currentTimeMillis();
		// System.out.println("t1 = " + (time1 - time));
		ViewHelper.drawMyLocationArrow(gc, x0, y0, viewParams, myLocation,
				myLocationDimmed);

		ViewHelper.drawCross(gc, x0, y0);
		ViewHelper.drawScale(gc, viewParams);
		// long time2 = System.currentTimeMillis();
		// System.out.println("t2 = " + (time2 - time1));

		LocationX targ = getTarget();

		if (targ != null) {
			ViewHelper.drawLine(gc, x0, y0, viewParams, getLocation(), targ,
					COLOR.dimm(COLOR.TARG_COLOR));
			if (myLocation != null)
				ViewHelper.drawLine(gc, x0, y0, viewParams, myLocation, targ,
						COLOR.dimm(COLOR.ARROW_COLOR));
		}
	}

	private final RectInt screenRect = new RectInt();

	private void drawTiles(GC gc, int x0, int y0) {
		tilesDrawn.clear();

		getScreenRectInt(screenRect);

		int nx0 = screenRect.getX() / Adapter.TILE_SIZE;
		int ny0 = screenRect.getY() / Adapter.TILE_SIZE;
		int nx1 = (screenRect.getX() + screenRect.getW()) / Adapter.TILE_SIZE;
		int ny1 = (screenRect.getY() + screenRect.getH()) / Adapter.TILE_SIZE;

		x0 = (int) (nx0 * Adapter.TILE_SIZE - viewParams.centerX() + x0);
		y0 = (int) (ny0 * Adapter.TILE_SIZE - viewParams.centerY() + y0);

		int centerX = (int) viewParams.centerX();
		int centerY = (int) viewParams.centerY();

		gc.setAntiAlias(true);

		int x = x0;
		for (int nx = nx0; nx <= nx1; nx++, x += Adapter.TILE_SIZE) {
			int y = y0;
			for (int ny = ny0; ny <= ny1; ny++, y += Adapter.TILE_SIZE) {
				long id = TileId.make(nx, ny, getZoom());
				tileDrawer.drawTile(gc, centerX, centerY, id, x, y,
						platformView.loadDuringScrolling() || !scrolling,
						viewParams.getZoom(), viewParams.getPrevZoom());
				tilesDrawn.add(id);
			}
		}
	}

	private void repaint() {
		platformView.repaint();
	}

	@Override
	public ISelectable getSel() {
		return sel;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void incInfoLevel() {
		envir.adapter.assertUIThread();
		if (infoLevel.ordinal() < InfoLevel.values().length - 1) {
			infoLevel = InfoLevel.values()[infoLevel.ordinal() + 1];
			invalidateTiles();
			updateSel();
		}
	}

	@Override
	public void decInfoLevel() {
		envir.adapter.assertUIThread();
		if (infoLevel.ordinal() > 0) {
			infoLevel = InfoLevel.values()[infoLevel.ordinal() - 1];
			invalidateTiles();
			updateSel();
		}
	}

	@Override
	public void setInfoLevel(InfoLevel level) {
		infoLevel = level;
		invalidateTiles();
		updateSel();
	}

	@Override
	public InfoLevel getInfoLevel() {
		return infoLevel;
	}

	@Override
	public void invalidateTiles() {
		envir.adapter.assertUIThread();
		// viewParams.reset();
		if (tiles != null) {
			tiles.cancelLoading();
			tiles.setInvalidAll();
			repaint();
		}
	}

	@Override
	public LocationX getTarget() {
		return envir.placemarks.getTarget();
	}

	@Override
	public LocationX getLocation() {
		envir.adapter.assertUIThread();
		return viewParams.getLocation();
	}

//	public LocationX getLocation(int dx, int dy) {
//		envir.adapter.assertUIThread();
//		return new LocationX(viewParams.scr2lon(dx, dy), viewParams.scr2lat(dx,
//				dy));
//	}

	private void cancelSel() {
		envir.adapter.assertUIThread();
		selectionThread.cancel();
	}

	private void select(ISelectable sel) {
		CommonView.this.sel = sel;
		CommonView.this.invalidateTiles();
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
		if (infoLevel != InfoLevel.HIGH
				|| rotationMode == RotationMode.ROTATION_GPS) {
			cancelSel();
			select(null);
			return;
		}
		selectionThread.set((int) viewParams.centerX(),
				(int) viewParams.centerY(), getScreenRect(null), getZoom(),
				envir.placemarks, envir.paths, selCallback);
	}

	private RectInt getScreenRectInt(RectInt rect) {
		if (rect == null)
			rect = new RectInt();

		int left = -getScreenCenterX();
		int right = platformView.getWidth() - getScreenCenterX();
		int top = -getScreenCenterY();
		int bottom = platformView.getHeight() - getScreenCenterY();

		int x1 = (int) viewParams.scr2geoX(left, top);
		int x2 = (int) viewParams.scr2geoX(right, bottom);
		int x3 = (int) viewParams.scr2geoX(right, top);
		int x4 = (int) viewParams.scr2geoX(left, bottom);
		int y1 = (int) viewParams.scr2geoY(left, top);
		int y2 = (int) viewParams.scr2geoY(right, bottom);
		int y3 = (int) viewParams.scr2geoY(right, top);
		int y4 = (int) viewParams.scr2geoY(left, bottom);

		int minX = Math.min(Math.min(x1, x2), Math.min(x3, x4));
		int maxX = Math.max(Math.max(x1, x2), Math.max(x3, x4));
		int minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
		int maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));

		rect.set(minX, minY, maxX - minX, maxY - minY);
		return rect;
	}

	private RectX getScreenRect(RectX rect) {
		if (rect == null)
			rect = new RectX(0, 0, 0, 0);

		int left = -getScreenCenterX();
		int right = platformView.getWidth() - getScreenCenterX();
		int top = -getScreenCenterY();
		int bottom = platformView.getHeight() - getScreenCenterY();

		double lon1 = viewParams.scr2lon(left, top);
		double lon2 = viewParams.scr2lon(right, bottom);
		double lon3 = viewParams.scr2lon(right, top);
		double lon4 = viewParams.scr2lon(left, bottom);
		double lat1 = viewParams.scr2lat(left, top);
		double lat2 = viewParams.scr2lat(right, bottom);
		double lat3 = viewParams.scr2lat(right, top);
		double lat4 = viewParams.scr2lat(left, bottom);

		double minLon = Math.min(Math.min(lon1, lon2), Math.min(lon3, lon4));
		double maxLon = Math.max(Math.max(lon1, lon2), Math.max(lon3, lon4));
		double minLat = Math.min(Math.min(lat1, lat2), Math.min(lat3, lat4));
		double maxLat = Math.max(Math.max(lat1, lat2), Math.max(lat3, lat4));

		rect.set(minLon, minLat, maxLon - minLon, maxLat - minLat);

		return rect;
	}

	@Override
	public void animateToMyLocation() {
		LocationX myLoc = getMyLocation();
		if (myLoc != null)
			animateTo(myLoc);
	}

	@Override
	public void animateToTarget() {
		LocationX target = getTarget();
		if (target != null)
			animateTo(target);
	}

	@Override
	public void onSizeChanged(int w, int h) {
		updateSel();
		repaint();
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~CommonView");
		super.finalize();
	}

	@Override
	public void fixMap(String map) {
		envir.maps.fixMap(map);
		invalidateTiles();
	}

}
