package kvv.aplayer.files.tape;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BobbinView extends ImageView {

	public BobbinView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BobbinView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BobbinView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setColor(0xFFE0FFFF);
		// setImageBitmap(BobbinBmp.createBmp(color));
	}

	public void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
		// int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		int size = getMeasuredWidth();
		setMeasuredDimension(size, size);
	}

	public void setColor(int color) {
		setImageBitmap(BobbinBmp.createBmp(color));
	}

}
