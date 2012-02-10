package kvv.kvvmap.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class KvvMapsButton extends Button {

	public KvvMapsButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(false);
//		setMinHeight(60);
//		setMinWidth(60);
	}

	private int bg;
	private int bgChecked;
	private int bgDisabled;

	public void setup(int bg, int bgChecked, int bgDisabled) {
		this.bg = bg;
		this.bgChecked = bgChecked;
		this.bgDisabled = bgDisabled;
	}

	private boolean checked;
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setBackgroundResource(enabled ? (checked ? bgChecked : bg) : bgDisabled);
	}
	
	public void check(boolean cheched) {
		this.checked = cheched;
		setBackgroundResource(isEnabled() ? (checked ? bgChecked : bg) : bgDisabled);
	}
}
