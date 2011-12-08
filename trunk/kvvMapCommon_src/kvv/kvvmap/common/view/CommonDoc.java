package kvv.kvvmap.common.view;

import java.util.Collections;
import java.util.List;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.adapter.PointInt;
import kvv.kvvmap.common.InfoLevel;
import kvv.kvvmap.common.LongSet;
import kvv.kvvmap.common.Utils;
import kvv.kvvmap.common.maptiles.MapTiles;
import kvv.kvvmap.common.pacemark.IPlaceMarksListener;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pathtiles.PathTiles;
import kvv.kvvmap.common.tiles.Tile;
import kvv.kvvmap.common.tiles.TileId;

public final class CommonDoc {

	private final ICommonView view;


	private final Environment envir;

	public CommonDoc(ICommonView view, final Environment envir) {
		this.envir = envir;
		this.view = view;

	}


}
