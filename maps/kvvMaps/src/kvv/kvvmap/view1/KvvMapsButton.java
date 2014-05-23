package kvv.kvvmap.view1;

import kvv.kvvmap.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class KvvMapsButton extends ImageButton {

	public KvvMapsButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(false);
	}

	private int img;
	private int imgDisabled;

	private int bgDefault = R.drawable.bg;

	public KvvMapsButton setup(int img, int imgDisabled) {
		this.img = img;
		this.imgDisabled = imgDisabled;
		return this;
	}

	public KvvMapsButton setup(int img) {
		this.img = this.imgDisabled = img;
		return this;
	}

	private boolean checked;

	private void setImage() {
		int id = isEnabled() ? img : imgDisabled;
		if (checked)
			setBackgroundResource(R.drawable.bg_on);
		else
			setBackgroundResource(bgDefault);
		setImageResource(id);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setImage();
	}

	public void setCheched(boolean cheched) {
		this.checked = cheched;
		bgDefault = R.drawable.bg_off;
		setImage();
	}
}
