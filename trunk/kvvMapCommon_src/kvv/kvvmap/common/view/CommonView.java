package kvv.kvvmap.common.view;

import java.util.Collections;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.adapter.RectInt;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.LongSet;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maps.Maps.MapsListener;
import kvv.kvvmap.common.maptiles.MapTiles;
import kvv.kvvmap.common.pacemark.IPlaceMarksListener;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.pathtiles.PathTiles;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileId;
import kvv.kvvmap.common.tiles.Tiles;

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
		envir.maps.setListener(new MapsListener() {
			@Override
			public void mapAdded(String name) {
				if (mapTiles != null) {
					mapTiles.stopLoading();
					mapTiles.setInvalidAll();
					Adapter.log("mapAdded notified");
					repaint();
				}
			}
		});

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

				drawTile(gc, mapTiles, centerXY, id, x, y);

				if (getInfoLevel().ordinal() > 0)
					drawTile(gc, pathTiles, centerXY, id, x, y);

				tilesDrawn.add(id);
			}
		}

		gc.clearTransform();
	}

	private final RectInt src = new RectInt();
	private final RectInt dst = new RectInt();

	private void drawTile(GC gc, Tiles tiles, PointInt centerXY, long id,
			int x, int y) {

		int _sz = Adapter.TILE_SIZE;
		int _x = 0;
		int _y = 0;
		int _nx = TileId.nx(id);
		int _ny = TileId.ny(id);
		int _z = TileId.zoom(id);

		Tile tile = tiles.getTile(id, centerXY, !scrolling);
		if (tile != null) {
			src.set(_x, _y, _sz, _sz);
			dst.set(x, y, Adapter.TILE_SIZE, Adapter.TILE_SIZE);
			tile.draw(gc, src, dst);
			return;
		}

		if (mapPos.getZoom() > mapPos.getPrevZoom()) {
			while (_z > Utils.MIN_ZOOM) {

				_sz /= 2;

				if ((_nx & 1) != 0)
					_x += _sz;
				if ((_ny & 1) != 0)
					_y += _sz;

				_nx /= 2;
				_ny /= 2;
				_z--;

				tile = tiles
						.getTile(TileId.make(_nx, _ny, _z), centerXY, false);
				if (tile != null) {
					src.set(_x, _y, _sz, _sz);
					dst.set(x, y, Adapter.TILE_SIZE, Adapter.TILE_SIZE);
					tile.draw(gc, src, dst);
					return;
				}
			}
		}
		if (mapPos.getZoom() < mapPos.getPrevZoom() && _z < Utils.MAX_ZOOM) {
			tile = tiles.getTile(TileId.make(_nx * 2, _ny * 2, _z + 1),
					centerXY, false);
			if (tile != null) {
				src.set(_x, _y, _sz, _sz);
				dst.set(x, y, Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = tiles.getTile(TileId.make(_nx * 2 + 1, _ny * 2, _z + 1),
					centerXY, false);
			if (tile != null) {
				src.set(_x, _y, _sz, _sz);
				dst.set(x + Adapter.TILE_SIZE / 2, y, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = tiles.getTile(TileId.make(_nx * 2, _ny * 2 + 1, _z + 1),
					centerXY, false);
			if (tile != null) {
				src.set(_x, _y, _sz, _sz);
				dst.set(x, y + Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = tiles.getTile(TileId.make(_nx * 2 + 1, _ny * 2 + 1, _z + 1),
					centerXY, false);
			if (tile != null) {
				src.set(_x, _y, _sz, _sz);
				dst.set(x + Adapter.TILE_SIZE / 2, y + Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
		}
	}

	// void drawTile(GC gc, Tiles tiles, PointInt centerXY, int id, RectInt src,
	// RectInt dst) {
	// Tile tile = tiles
	// .getTile(id, centerXY, false);
	// if (tile != null) {
	// tile.draw(gc, src, dst);
	// return;
	// }
	// }

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
			pathTiles.stopLoading();
			pathTiles.setInvalidAll();
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
