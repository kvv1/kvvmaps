package kvv.kvvmap.common.tiles;




public class TileId {
	public static long get(int nx, int ny, int zoom) {
		long l = ((long) zoom << 32) + ((long) (nx & 0xFFFF) << 16) + (ny & 0xFFFF);
		
//		int _nx = nx(l);
//		int _ny = ny(l);
//		int _zoom = zoom(l);
//		
//		if(_nx != nx || _ny != ny || _zoom != zoom)
//			throw new RuntimeException("error in TileId");
		
		return l;
	}

	public static int zoom(long id) {
		int zoom = (int) (id >>> 32);
		return zoom;
	}

	public static int nx(long id) {
		return ((int) (id >>> 16)) & 0xFFFF;
	}

	public static int ny(long id) {
		return (int) ((int) id & 0xFFFF);
	}

	public static String toString(long id) {
		return String.format("[Tile: nx=%d ny=%d z=%d]", nx(id), ny(id), zoom(id));
	}
}