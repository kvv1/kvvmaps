package kvv.kvvmap.view;

import kvv.kvvmap.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class KvvMapsButton extends ImageButton {

	public KvvMapsButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(false);

//		if (attrs.getAttributeValue(
//				"http://schemas.android.com/apk/res/android", "minWidth") == null)
//			setMinimumWidth(40);
//		if (attrs.getAttributeValue(
//				"http://schemas.android.com/apk/res/android", "minHeight") == null)
//			setMinimumHeight(40);

		// setAlpha(128);

		// setBackgroundColor(0x80000000);

		// setBackgroundResource(attrs.getAttributeResourceValue(null, "Src",
		// 0));
		// setBackgroundResource(R.drawable.bg);
		// setImageResource(R.drawable.gps_on);

		// setMinHeight(60);
		// setMinWidth(60);
	}

	private int bg;
	private int bgChecked;
	private int bgDisabled;

	public KvvMapsButton setup(int bg, int bgChecked, int bgDisabled) {
		this.bg = bg;
		this.bgChecked = bgChecked;
		this.bgDisabled = bgDisabled;
		return this;
	}

	private boolean checked;

	private void setImage() {
		int id = isEnabled() ? (checked ? bgChecked : bg) : bgDisabled;
		if (checked)
			setBackgroundResource(R.drawable.bg_on);
		else
			setBackgroundResource(R.drawable.bg);
		setImageResource(id);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setImage();
	}

	public void setCheched(boolean cheched) {
		this.checked = cheched;
		setImage();
	}
}
