package kvv.kvvmap.view;

import kvv.kvvmap.placemark.PathSelection;


public interface IPlatformView {
	public void repaint();
	int getWidth();
	int getHeight();
	void getLocationOnScreen(int[] res);
	public void pathSelected(PathSelection sel);
}
