package kvv.aplayer.files.tape;

import kvv.aplayer.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
		ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeGlobalOnLayoutListener(this);

				BobbinView b1 = (BobbinView) findViewById(R.id.leftBobbin);
				BobbinView b2 = (BobbinView) findViewById(R.id.rightBobbin);

				TapeView tapeView = (TapeView) findViewById(R.id.tape);
				tapeView.setBobbinParams(b1, b2);
			}
		});
	}

	@SuppressLint("NewApi")
	private boolean hitTest(float x, float y, int bobbin) {
		BobbinView b1 = (BobbinView) findViewById(R.id.leftBobbin);
		BobbinView b2 = (BobbinView) findViewById(R.id.rightBobbin);
		
		float bobbinSize = b1.getWidth();
		float bobbinX1 = b1.getX() + bobbinSize / 2;
		float bobbinX2 = b2.getX() + bobbinSize / 2;
		float bobbinY = b1.getY() + bobbinSize / 2;
		
		
		float w = getWidth();
		float bx = bobbin < 0 ? bobbinX1 : bobbinX2;
		float by = bobbinY;
		
		if(y > bobbinSize * 3 / 4)
			return false;
		
		if(bobbin == -1 && x < bx + bobbinSize/4)
			return true;
		
		if(bobbin == 1 && x > bx - bobbinSize/4)
			return true;

		return false;
		
		//return Math.sqrt(((x - bx) * (x - bx) + (y - by) * (y - by))) < bobbinSize * 0.2;
	}

	public int hitTest(float x, float y) { // -1 - left, 1 = right
		if (hitTest(x, y, -1))
			return -1;
		if (hitTest(x, y, 1))
			return 1;

		// if (x < getWidth() / 5 && y < h / 5)
		// return -2;
		//
		// if (x > getWidth() - getWidth() / 5 && y < h / 5)
		// return 2;

		return 0;
	}

}
