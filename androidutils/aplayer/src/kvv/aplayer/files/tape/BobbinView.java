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
		 setImageBitmap(BobbinBmp.getInstance());
	}
	
//	Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//
//	@Override
//	protected void onDraw(Canvas canvas) {
//
//		canvas.drawBitmap(BobbinBmp.getInstance(), null, new RectF(0, 0,
//				getWidth(), getWidth()), paint);
//	}

	public void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
		//int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		int size = getMeasuredWidth();
		setMeasuredDimension(size, size);
	}
}
