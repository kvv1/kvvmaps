package kvv.kvvmap.common.pacemark;



public interface IPlaceMarksListener {
	void onPathTileChanged(long tileid);
	void onPathTilesChanged();
	void onPathTilesChangedAsync();
}
