package kvv.kvvmap.common.view;

import kvv.kvvmap.common.pacemark.ISelectable;


public interface IPlaceMarksListener {
	void onPathTileChanged(long id);
	void onPathTilesChanged();
	//void onPathTileChangedAsync(long id);
	ISelectable getSelAsync();
	void updateSel();
	void exec(Runnable r);
}
