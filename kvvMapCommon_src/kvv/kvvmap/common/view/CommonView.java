package kvv.kvvmap.common.view;

import java.util.Collections;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
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

	private InfoLevel infoLevel = InfoLevel.HIGH;

	private final Tiles tiles;

	private final SelectionThread selectionThread = new SelectionThread();
	private final LongSet tilesDrawn = new LongSet();

	private LocationX myLocation;

	private boolean myLocationDimmed;

	private final Environment envir;

	private final Diagram diagram;

	private boolean scrolling;

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
				
				long t = System.currentTimeMillis();
				
				Img img = envir.maps.load(TileId.nx(id), TileId.ny(id),
						TileId.zoom(id), content);
				if(img == null)
					return null;

				long t1 = System.currentTimeMillis();
				t = t1 - t;
				
				createPathsImg(id, img.img);
				
				 t1 = System.currentTimeMillis() - t1;
				Adapter.log("load tile " + t + " " + t1);

				return new Tile(envir.adapter, id, img, content);
			}
		}, Adapter.MAP_TILES_CACHE_SIZE) {
			@Override
			protected void loaded(Tile tile) {
				repaint();
			}
		};

		diagram = new Diagram(envir.adapter, this);
	}

	private Img createPathsImg(long id, Object img1) {
		if (img1 == null)
			return null;

		GC gc = envir.adapter.getGC(img1);

		if (Adapter.debugDraw) {
			gc.setColor(COLOR.RED);
			gc.drawRect(10, 10, 235, 235);
			gc.drawText(TileId.toString(id), 20, 20);
			gc.drawText("mem " + Runtime.getRuntime().freeMemory() / 1024
					/ 1024 + " " + Runtime.getRuntime().totalMemory() / 1024
					/ 1024, 20, 40);
		}

		InfoLevel infoLevel = getInfoLevel();
		if (infoLevel.ordinal() > 0) {
			gc.setAntiAlias(true);
			ISelectable sel = CommonView.this.sel;
			PathDrawer.drawPaths(envir.paths, gc, id, infoLevel, sel);
			PathDrawer.drawPlacemarks(envir.placemarks, gc, id, infoLevel, sel);
		}
		return new Img(img1, true);
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
			double dx = mapPos.geo2scrX(myLocation.getX(mapPos.getZoom()),
					myLocation.getY(mapPos.getZoom()))
					- mapPos.geo2scrX(oldLocation.getX(mapPos.getZoom()),
							oldLocation.getY(mapPos.getZoom()));
			double dy = mapPos.geo2scrY(myLocation.getX(mapPos.getZoom()),
					myLocation.getY(mapPos.getZoom()))
					- mapPos.geo2scrY(oldLocation.getX(mapPos.getZoom()),
							oldLocation.getY(mapPos.getZoom()));
			animateBy(dx, dy);
			repaint();
		} else if (scroll) {
			animateTo(myLocation);
			repaint();
		}

	}

	private boolean onScreen(LocationX loc) {
		return loc != null
				&& (Math.abs(mapPos.geo2scrX(loc.getX(mapPos.getZoom()),
						loc.getY(mapPos.getZoom()))) < platformView.getWidth() / 2 && Math
						.abs(mapPos.geo2scrY(loc.getX(mapPos.getZoom()),
								loc.getY(mapPos.getZoom()))) < platformView
						.getHeight() / 2);
	}

	public void dimmMyLocation() {
		myLocationDimmed = true;
	}

	public boolean isOnMyLocation() {
		return myLocation != null
				&& Math.abs(mapPos.geo2scrX(myLocation.getX(mapPos.getZoom()),
						myLocation.getY(mapPos.getZoom()))) < 2
				&& Math.abs(mapPos.geo2scrY(myLocation.getX(mapPos.getZoom()),
						myLocation.getY(mapPos.getZoom()))) < 2;
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

	public void setZoom(int zoom) {
		envir.adapter.assertUIThread();
		tiles.cancelLoading();
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

	public void animateBy(double dx, double dy) {
		envir.adapter.assertUIThread();
		mapPos.animateBy(dx, dy);
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

		if (!scrolling) {
			LocationX myLoc = myLocation;
			if (!isMyLocationDimmed())
				myLoc = null;
			ViewHelper
					.drawTarget(gc, mapPos, getLocation(), myLoc, getTarget());
		}

		LocationX targ = getTarget();
		if (targ != null) {
			ViewHelper.drawLine(gc, mapPos, getLocation(), targ,
					COLOR.dimm(COLOR.TARG_COLOR));
			if (myLocation != null)
				ViewHelper.drawLine(gc, mapPos, myLocation, targ,
						COLOR.dimm(COLOR.ARROW_COLOR));
		}
	}

	private static double x_tiles2scr(double x, int w, double centerx) {
		x -= centerx;
		return x + w / 2;
	}

	private static double y_tiles2scr(double y, int h, double centery) {
		y -= centery;
		return y + h / 2;
	}

	private void drawTiles(GC gc) {
		tilesDrawn.clear();

		PointInt centerXY = new PointInt((int) mapPos.centerX(),
				(int) mapPos.centerY());

		int w = gc.getWidth();
		int h = gc.getHeight();

		int nx0 = (int) (mapPos.centerX() - w / 2) / Adapter.TILE_SIZE;
		int ny0 = (int) (mapPos.centerY() - h / 2) / Adapter.TILE_SIZE;
		int x0 = (int) x_tiles2scr(nx0 * Adapter.TILE_SIZE, w, mapPos.centerX());
		int y0 = (int) y_tiles2scr(ny0 * Adapter.TILE_SIZE, h, mapPos.centerY());

		int[] scrLoc = new int[2];
		platformView.getLocationOnScreen(scrLoc);

		gc.setTransform((float) (mapPos.angle() * 180 / Math.PI), w / 2
				+ scrLoc[0], h / 2 + scrLoc[1]);

		gc.setAntiAlias(true);

		int x = x0;
		for (int nx = nx0; x < w; nx++, x += Adapter.TILE_SIZE) {
			int y = y0;
			for (int ny = ny0; y < h; ny++, y += Adapter.TILE_SIZE) {
				long id = TileId.make(nx, ny, getZoom());

				tiles.drawTile(gc, centerXY, id, x, y,
						platformView.loadDuringScrolling() || !scrolling,
						mapPos.getZoom(), mapPos.getPrevZoom());

//				if (getInfoLevel().ordinal() > 0)
//					pathTiles.drawTile(gc, centerXY, id, x, y,
//							platformView.loadDuringScrolling() || !scrolling,
//							mapPos.getZoom(), mapPos.getPrevZoom());

				tilesDrawn.add(id);
			}
		}

		gc.clearTransform();
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
		return new LocationX(mapPos.scrX2lon(dx, dy), mapPos.scrY2lat(dx, dy));
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