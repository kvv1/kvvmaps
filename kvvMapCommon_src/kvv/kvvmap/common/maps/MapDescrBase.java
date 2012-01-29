package kvv.kvvmap.common.maps;

import java.util.Collection;

import kvv.kvvmap.adapter.Adapter;
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

	protected abstract void load(int nx, int ny, int zoom, int x, int y,
			int sz, Img imgBase);

	public static Img load(Collection<MapDescrBase> maps,
			MapDescrBase fixedMap, int nx, int ny, int zoom,
			TileContent content, Adapter adapter) {

		Img img = new Img(adapter.allocBitmap(), true);
		if(img.img == null)
			return null;

		//Object bm = adapter.allocBitmap(Utils.TILE_SIZE_G, Utils.TILE_SIZE_G);
		
		if (fixedMap != null) {
			int x = 0;
			int y = 0;
			int sz = Utils.TILE_SIZE_G;
			int nx1 = nx;
			int ny1 = ny;
			int zoom1 = zoom;

			while (zoom1 >= Utils.MIN_ZOOM && img.transparent) {
				fixedMap.loadInZoom(nx1, ny1, zoom1, x, y, sz, img, null);
				x = x /  2 + ((nx1 & 1) << 7);
				y = y / 2 + ((ny1 & 1) << 7);
				nx1 >>>= 1;
				ny1 >>>= 1;
				zoom1--;
				sz >>>= 1;
			}
		}

		int x = 0;
		int y = 0;
		int sz = Utils.TILE_SIZE_G;

		while (zoom >= Utils.MIN_ZOOM && img.transparent) {
			for (MapDescrBase map : maps)
				map.loadInZoom(nx, ny, zoom, x, y, sz, img, content);
			x = x / 2 + ((nx & 1) << 7);
			y = y / 2 + ((ny & 1) << 7);
			nx >>>= 1;
			ny >>>= 1;
			zoom = zoom - 1;
			sz >>>= 1;
		}


		//img.transparent = adapter.isTransparent(img.img);
		
		return img;
	}

	private Img loadInZoom(int nx, int ny, int zoom, int x, int y, int sz,
			Img img, TileContent content) {
		if (hasTile(nx, ny, zoom)) {
			load(nx, ny, zoom, x, y, sz, img);
			if (content != null && (content.zoom == -1 || content.zoom == zoom)) {
				content.zoom = zoom;
				content.maps.add(name);
			}
		}
		return img;
	}
	
	private static final long table[] = {
		0x0000000000000000L,
		0x0303000000000000L,
		0x0C0C000000000000L,
		0x0F0F000000000000L,
		
		0x3030000000000000L,
		0x3333000000000000L,
		0x3C3C000000000000L,
		0x3F3F000000000000L,
		
		0xC0C0000000000000L,
		0xC3C3000000000000L,
		0xCCCC000000000000L,
		0xCFCF000000000000L,
		
		0xF0F0000000000000L,
		0xF3F3000000000000L,
		0xFCFC000000000000L,
		0xFFFF000000000000L,
	};
	
	private static long zoom(long oldmask, int shift) {
		oldmask >>>= shift;

		long res = 0;

		res = table[(int)oldmask & 0x0F];
		oldmask >>>= 8;
		res = (res >>> 16) | table[(int)oldmask & 0x0F];
		oldmask >>>= 8;
		res = (res >>> 16) | table[(int)oldmask & 0x0F];
		oldmask >>>= 8;
		res = (res >>> 16) | table[(int)oldmask & 0x0F];
		
		return res;
	}

	private static void printMask(long mask) {
		for(int y = 0; y < 8; y++) {
			System.out.println();
			for(int x = 0; x < 8; x++) {
				if((mask & 0x8000000000000000L) != 0)
					System.out.print("x ");
				else
					System.out.print(". ");
				mask <<= 1;
			}
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		long n = 7543788578875838597L;
		printMask(n);

		printMask(zoom(n, 4));
	}

}
