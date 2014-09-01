package kvv.kvvmap.maps;

import java.util.LinkedList;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.util.Img;

public final class Tile {
	private final Adapter adapter;
	public final long id;
	public Img img;
	public int zoom = -1;
	public LinkedList<String> maps;
	public boolean expired;
	
	private static int cnt;

	public Tile(Adapter adapter, long id, Img img) {
		this.adapter = adapter;
		this.id = id;
		this.img = img;
		Adapter.log("Tile " + ++cnt);
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~Tile " + --cnt);
		super.finalize();
	}

	public boolean isMultiple() {
		return maps != null && maps.size() > 1;
	}
	
	public synchronized void dispose() {
		Adapter.log("Tile disposed " + cnt);
		adapter.recycleBitmap(img.img);
		img = null;
	}
	
}
