package kvv.kvvmap.view;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.GC;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.view.Diagram;
import kvv.kvvmap.common.view.IPlatformView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DiagramView extends View implements IPlatformView {

	private Diagram diagram;
	public boolean speedProfile;

	public DiagramView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(Adapter adapter) {
		diagram = new Diagram(adapter, this);
	}

	public void pathSelected(PathSelection sel) {
		Adapter.log("diagram " + getWidth() + " " + getHeight());

		int w = getWidth();
		int h = getHeight();

		if (diagram != null)
			diagram.set(sel, w, h, speedProfile);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();

		if (diagram != null)
			diagram.draw(new GC(canvas, new Paint(), w, h), 0);

		// Paint paint = new Paint();
		// canvas.drawLine(0, 0, w, h, paint);
		// canvas.drawLine(0, h, w, 0, paint);
	}

	@Override
	public void repaint() {
		postInvalidate();
	}

	@Override
	public boolean loadDuringScrolling() {
		return false;
	}

	@Override
	protected void finalize() throws Throwable {
		Adapter.log("~DiagramView");
		super.finalize();
	}

	public void dispose() {
		if (diagram != null)
			diagram.dispose();
		diagram = null;
	}
}
