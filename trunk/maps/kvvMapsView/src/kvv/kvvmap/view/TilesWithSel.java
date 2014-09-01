package kvv.kvvmap.view;

import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.placemark.IPlaceMarksListener;
import kvv.kvvmap.placemark.PathDrawer;
import kvv.kvvmap.tiles.Tiles;
import kvv.kvvmap.util.ISelectable;
import kvv.kvvmap.util.Img;
import kvv.kvvmap.util.InfoLevel;

public abstract class TilesWithSel extends Tiles {

	private final Environment envir;
	
	private ISelectable sel;
	private volatile InfoLevel infoLevel = InfoLevel.HIGH;

	public InfoLevel getInfoLevel() {
		return infoLevel;
	}

	public ISelectable getSel() {
		return sel;
	}
	
	public void setInfoLevel(InfoLevel level) {
		infoLevel = level;
		setInvalidAll();
	}

	public TilesWithSel(final Environment envir) {
		super(envir.adapter, envir.maps);
		this.envir = envir;
		envir.placemarks.setListener(pmListener);
		envir.paths.setListener(pmListener);
	}

	public void select(ISelectable sel) {
		this.sel = sel;
		setInvalidAll();
	}

	@Override
	protected void drawAdditionalsAsync(long id, Img img) {
		GC gc = envir.adapter.getGC(img.img);

		if (infoLevel.ordinal() > 0) {
			gc.setAntiAlias(true);
			PathDrawer.drawPaths(envir.paths, gc, id, infoLevel, sel);
			PathDrawer.drawPlacemarks(envir.placemarks, gc, id,
					infoLevel, sel, envir.adapter.getScaleFactor());
		}
	}

	private final IPlaceMarksListener pmListener = new IPlaceMarksListener() {

		@Override
		public void onPathTilesChanged() {
			setInvalidAll();
		}

		@Override
		public void onPathTileChanged(long id) {
			envir.adapter.assertUIThread();
			setInvalid(id);
		}

		private Runnable r = new Runnable() {
			@Override
			public void run() {
				onPathTilesChanged();
			}
		};

		@Override
		public void onPathTilesChangedAsync() {
			envir.adapter.execUI(r);
		}
	};

}
