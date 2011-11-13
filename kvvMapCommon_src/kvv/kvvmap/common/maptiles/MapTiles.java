package kvv.kvvmap.common.maptiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.maps.Maps;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileLoader.TileLoaderCallback;
import kvv.kvvmap.common.tiles.Tiles;

public abstract class MapTiles extends Tiles {
	private final Maps maps;

	public MapTiles(Adapter adapter, Maps maps, int cacheSize) {
		super(adapter, cacheSize);
		this.maps = maps;
	}

	@Override
	protected void load(Long id, final TileLoaderCallback callback,
			PointInt prioLoc) {
		maps.load(id, callback, prioLoc);
	}

	public final void reorder(Tile tile) {
		maps.reorder(tile.content.maps.getLast());
		setInvalidAll();
	}

	public final void setTopMap(String map) {
		maps.setTopMap(map);
		setInvalidAll();
	}

	public void stopLoading() {
		maps.cancelLoading();
	}

}
