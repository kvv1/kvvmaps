package kvv.kvvmap.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class OnScreenButton extends ImageButton{

	public OnScreenButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(0);
		setFocusable(false);
		setEnabled(true);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(enabled)
			setAlpha(128);
		else
			setAlpha(32);
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		super.onTouchEvent(event);
//		return false;
//	}
}
