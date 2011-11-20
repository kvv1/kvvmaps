package kvv.kvvmap.common.pathtiles;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.Img;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.PathDrawer;
import kvv.kvvmap.common.pacemark.Paths;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileId;
import kvv.kvvmap.common.tiles.TileLoader;
import kvv.kvvmap.common.tiles.TileLoader.TileLoaderCallback;
import kvv.kvvmap.common.tiles.Tiles;
import kvv.kvvmap.common.view.CommonDoc;

public abstract class PathTiles extends Tiles {

	private final PlaceMarks placemarks;
	private final Paths paths;
	private final Adapter adapter;
	private final TileLoader tileLoader;

	public PathTiles(final Adapter adapter, PlaceMarks placemarks, Paths paths,
			int cacheSize) {
		super(adapter, cacheSize);
		this.adapter = adapter;
		this.placemarks = placemarks;
		this.paths = paths;
		this.tileLoader = new TileLoader(adapter) {
			@Override
			protected Tile loadAsync(long id) {
				Img img = createPathsImg(id);

				if (img == null)
					return null;

				return new Tile(adapter, id, img, null);
			}
		};
	}

	@Override
	protected void load(Long id, final TileLoaderCallback callback,
			PointInt centerXY) {
		tileLoader.load(id, callback, centerXY);
	}

	protected abstract InfoLevel getInfoLevel();

	protected abstract ISelectable getSelAsync();

	private Img createPathsImg(long id) {
		Object img1 = adapter.allocBitmap();
		if (img1 == null)
			return null;

		GC gc = adapter.getGC(img1);

		if (CommonDoc.debugDraw) {
			gc.setColor(COLOR.RED);
			gc.drawRect(10, 10, 235, 235);
			gc.drawText(TileId.toString(id), 20, 20);
			gc.drawText("mem " + Runtime.getRuntime().freeMemory() / 1024
					/ 1024 + " " + Runtime.getRuntime().totalMemory() / 1024
					/ 1024, 20, 40);
		}

		InfoLevel infoLevel = getInfoLevel();
		if (infoLevel.ordinal() > 0) {
			gc.setAntiAlias(true);
			ISelectable sel = getSelAsync();
			PathDrawer.drawPaths(paths, gc, id, infoLevel, sel);
			PathDrawer.drawPlacemarks(placemarks, gc, id, infoLevel, sel);
		}
		return new Img(img1, true);
	}

	public void stopLoading() {
		tileLoader.cancelLoading();
	}

}
