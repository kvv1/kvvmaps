package kvv.kvvmap.common.view;


public interface IPlatformView {
	public void repaint();
	int getWidth();
	int getHeight();
	void getLocationOnScreen(int[] res);
	boolean loadDuringScrolling();
}
