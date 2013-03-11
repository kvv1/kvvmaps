package kvv.kvvmap.common.view;

import kvv.kvvmap.common.pacemark.PathSelection;


public interface IPlatformView {
	public void repaint();
	int getWidth();
	int getHeight();
	void getLocationOnScreen(int[] res);
	boolean loadDuringScrolling();
	public void pathSelected(PathSelection sel);
}
