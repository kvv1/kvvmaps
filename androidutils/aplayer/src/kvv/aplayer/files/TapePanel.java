package kvv.aplayer.files;

import kvv.aplayer.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class TapePanel extends FrameLayout {

	public TapePanel(Context context) {
		super(context);
		createView(context);
	}

	public TapePanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		createView(context);
	}

	public TapePanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createView(context);
	}

	private void createView(Context context) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.view_tape, null, true);
//		View view = inflate(getContext(), R.layout.view_tape, null);
		addView(view);
	}
	
}
