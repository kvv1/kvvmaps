package kvv.kvvmap.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.maps.Tile;
import kvv.kvvmap.util.TileId;

public abstract class TileLoader extends AbstractLoader<Long, Tile> {
	private final Adapter adapter;
	private int centerX;
	private int centerY;

	public TileLoader(Adapter adapter) {
		this.adapter = adapter;
	}

	@Override
	protected void execUI(Runnable r) {
		adapter.execUI(r);
	}

	@Override
	protected void execBG(Runnable r) {
		adapter.execBG(r);
	}

	@Override
	protected int getPrio(Long id) {
		return Math.abs(TileId.nx(id) * Adapter.TILE_SIZE + Adapter.TILE_SIZE
				/ 2 - centerX)
				+ Math.abs(TileId.ny(id) * Adapter.TILE_SIZE
						+ Adapter.TILE_SIZE / 2 - centerY);
	}

	public void load(Long id, int centerX, int centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
		load(id);
	}
}
