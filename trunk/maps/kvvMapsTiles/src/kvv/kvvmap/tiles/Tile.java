package kvv.kvvmap.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.util.Img;

public final class Tile {
	private final Adapter adapter;
	public final long id;
	public Img img;
	public final TileContent content;
	public boolean expired;
	
	private static int cnt;

	public Tile(Adapter adapter, long id, Img img, TileContent context) {
		this.adapter = adapter;
		this.id = id;
		this.img = img;
		this.content = context;
		Adapter.log("Tile " + ++cnt);
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Tile " + --cnt);
		super.finalize();
	}

	public boolean isMultiple() {
		return content != null && content.maps.size() > 1;
	}
	
	public synchronized void dispose() {
		Adapter.log("Tile disposed " + cnt);
		adapter.recycleBitmap(img.img);
		img = null;
	}
	
}
