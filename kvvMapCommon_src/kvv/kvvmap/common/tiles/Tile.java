package kvv.kvvmap.common.tiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.common.Img;

public final class Tile {
	private final Adapter adapter;
	public final long id;
	public Img img;
	public final TileContent content;
	public boolean needsReloading;

	public Tile(Adapter adapter, long id, Img img, TileContent context) {
		this.adapter = adapter;
		this.id = id;
		this.img = img;
		this.content = context;
	}

	public synchronized void draw(GC gc, int x, int y) {
		if (img != null)
			gc.drawImage(img.img, x, y);
	}

	public boolean isMultiple() {
		return content != null && content.maps.size() > 1;
	}
	
	public synchronized void dispose() {
		adapter.disposeBitmap(img.img);
		img = null;
	}
}
