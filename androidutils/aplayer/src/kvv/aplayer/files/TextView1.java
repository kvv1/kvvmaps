package kvv.aplayer.files;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextView1 extends TextView {

	public TextView1(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int horiz, int vert, int oldHoriz,
			int oldVert) {
		super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
		if (scrollListener != null)
			scrollListener.onScroll(horiz, vert, oldHoriz, oldVert);
	}

	public ScrollListener scrollListener;

	public interface ScrollListener {
		void onScroll(int horiz, int vert, int oldHoriz, int oldVert);
	}
}
