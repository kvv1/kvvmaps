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
	
	private static int cnt;

	public Tile(Adapter adapter, long id, Img img, TileContent context) {
		this.adapter = adapter;
		this.id = id;
		this.img = img;
		this.content = context;
		Adapter.log("Tile " + ++cnt);
	}

	public synchronized void draw(GC gc, int x, int y, int factor) {
		if (img != null)
			gc.drawImage(img.img, x, y, factor);
	}

	public boolean isMultiple() {
		return content != null && content.maps.size() > 1;
	}
	
	public synchronized void dispose() {
		adapter.disposeBitmap(img.img);
		img = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Tile " + --cnt);
		super.finalize();
	}
}
