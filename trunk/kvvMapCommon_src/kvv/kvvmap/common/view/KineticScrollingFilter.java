package kvv.kvvmap.common.view;

public interface KineticScrollingFilter {
	void onMousePressed(int x, int y);
	void onMouseReleased(int x, int y);
	void onMouseDragged(int x, int y);
	void exec(Runnable r);
	void mousePressed(int x, int y);
	void mouseReleased(int x, int y);
	void mouseDragged(int x, int y);
}
