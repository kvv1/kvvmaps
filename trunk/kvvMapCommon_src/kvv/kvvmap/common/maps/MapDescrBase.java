package kvv.kvvmap.common.maps;

import java.util.Collection;

import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.tiles.TileContent;

public abstract class MapDescrBase {

	private final String name;
	
	public MapDescrBase(String name) {
		this.name = name; 
	}

	public String getName() {
		return name;
	}
	
	protected abstract boolean hasTile(int nx, int ny, int zoom);
	protected abstract Img load(int nx, int ny, int zoom, int x, int y, int sz, Img imgBase);

	public static Img load(Collection<MapDescrBase> maps, MapDescrBase fixedMap,
			int nx, int ny, int zoom, TileContent content) {

		Img img = null;

		if (fixedMap != null) {
			int x = 0;
			int y = 0;
			int sz = Utils.TILE_SIZE_G;
			int nx1 = nx;
			int ny1 = ny;
			int z = zoom;

			while (true) {
				img = loadInZoom(fixedMap, nx1, ny1, z, x, y, sz, img, null);

				if (z <= Utils.MIN_ZOOM || (img != null && !img.transparent))
					break;

				x = x / 2 + ((nx1 & 1) << 7);
				y = y / 2 + ((ny1 & 1) << 7);
				nx1 = nx1 >>> 1;
				ny1 = ny1 >>> 1;
				z = z - 1;
				sz = sz / 2;
			}
		}

		int x = 0;
		int y = 0;
		int sz = Utils.TILE_SIZE_G;

		while (true) {
			for (MapDescrBase map : maps)
				img = loadInZoom(map, nx, ny, zoom, x, y, sz, img, content);

			if (zoom <= Utils.MIN_ZOOM || (img != null && !img.transparent))
				break;

			x = x / 2 + ((nx & 1) << 7);
			y = y / 2 + ((ny & 1) << 7);
			nx = nx >>> 1;
			ny = ny >>> 1;
			zoom = zoom - 1;
			sz = sz / 2;
		}

		return img;
	}

	private static Img loadInZoom(MapDescrBase map, int nx, int ny, int zoom,
			int x, int y, int sz, Img img, TileContent content) {
		if (map.hasTile(nx, ny, zoom)) {
			img = map.load(nx, ny, zoom, x, y, sz, img);
			if (content != null && (content.zoom == -1 || content.zoom == zoom)) {
				content.zoom = zoom;
				content.maps.add(map.getName());
			}
		}
		return img;
	}

}

