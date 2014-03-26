package kvv.kvvmap.placemark;



public interface IPlaceMarksListener {
	void onPathTileChanged(long tileid);
	void onPathTilesChanged();
	void onPathTilesChangedAsync();
}
