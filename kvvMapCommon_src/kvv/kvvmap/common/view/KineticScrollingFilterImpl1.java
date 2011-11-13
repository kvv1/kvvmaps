package kvv.kvvmap.common.view;


public abstract class KineticScrollingFilterImpl1 implements KineticScrollingFilter {

	@Override
	public void mousePressed(int x, int y) {
		onMousePressed(x, y);
	}

	@Override
	public void mouseReleased(int x, int y) {
		onMouseReleased(x, y);
	}

	@Override
	public void mouseDragged(int x, int y) {
		onMouseDragged(x, y);
	}

}
