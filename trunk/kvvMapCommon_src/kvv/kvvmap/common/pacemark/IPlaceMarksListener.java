package kvv.kvvmap.common.pacemark;



public interface IPlaceMarksListener {
	void onPathTileChanged(long id);
	void onPathTilesChanged();
	void updateSel();
	void exec(Runnable r);
}
