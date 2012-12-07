package kvv.kvvmap.common.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.RectInt;
import kvv.kvvmap.common.Utils;

public class TileDrawer {

	private final RectInt src = new RectInt();
	private final RectInt dst = new RectInt();

	private final Adapter adapter;
	private final Tiles tiles;
	
	public TileDrawer(Adapter adapter, Tiles tiles) {
		this.adapter = adapter;
		this.tiles = tiles;
	}
	
	public void drawTile(GC gc, int centerX, int centerY, long id, int x,
			int y, boolean loadIfNeeded, int zoom, int prevZoom) {
		adapter.assertUIThread();

		int _sz = Adapter.TILE_SIZE;
		int _x = 0;
		int _y = 0;
		int _nx = TileId.nx(id);
		int _ny = TileId.ny(id);
		int _z = TileId.zoom(id);

		Tile tile = tiles.getTile(id, centerX, centerY, loadIfNeeded);
		if (tile != null) {
			src.set(_x, _y, _sz, _sz);
			dst.set(x, y, Adapter.TILE_SIZE, Adapter.TILE_SIZE);
			tile.draw(gc, src, dst);
			return;
		}

		if (zoom > prevZoom) {
			while (_z > Utils.MIN_ZOOM) {

				if (_sz >= 2) {
					_x = _x / 2 + ((_nx & 1) * Adapter.TILE_SIZE / 2);
					_y = _y / 2 + ((_ny & 1) * Adapter.TILE_SIZE / 2);
					_sz /= 2;
				}

				_nx /= 2;
				_ny /= 2;
				_z--;

				tile = tiles.getTile(TileId.make(_nx, _ny, _z), centerX, centerY,
						false);
				if (tile != null) {
					src.set(_x, _y, _sz, _sz);
					dst.set(x, y, Adapter.TILE_SIZE, Adapter.TILE_SIZE);
					tile.draw(gc, src, dst);
					return;
				}
			}
		}
		if (zoom < prevZoom && _z < Utils.MAX_ZOOM) {
			src.set(0, 0, Adapter.TILE_SIZE, Adapter.TILE_SIZE);

			tile = tiles.getTile(TileId.make(_nx * 2, _ny * 2, _z + 1), centerX,
					centerY, false);
			if (tile != null) {
				dst.set(x, y, Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = tiles.getTile(TileId.make(_nx * 2 + 1, _ny * 2, _z + 1), centerX,
					centerY, false);
			if (tile != null) {
				dst.set(x + Adapter.TILE_SIZE / 2, y, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = tiles.getTile(TileId.make(_nx * 2, _ny * 2 + 1, _z + 1), centerX,
					centerY, false);
			if (tile != null) {
				dst.set(x, y + Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
			tile = tiles.getTile(TileId.make(_nx * 2 + 1, _ny * 2 + 1, _z + 1),
					centerX, centerY, false);
			if (tile != null) {
				dst.set(x + Adapter.TILE_SIZE / 2, y + Adapter.TILE_SIZE / 2,
						Adapter.TILE_SIZE / 2, Adapter.TILE_SIZE / 2);
				tile.draw(gc, src, dst);
			}
		}
	}

}
