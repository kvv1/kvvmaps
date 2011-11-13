package kvv.kvvmap.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class OnScreenButton extends ImageButton{

	public OnScreenButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAlpha(100);
		setBackgroundColor(0);
		setFocusable(false);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(enabled)
			setAlpha(255);
		else
			setAlpha(100);
	}
}
