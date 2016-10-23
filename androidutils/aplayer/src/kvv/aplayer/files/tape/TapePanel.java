package kvv.aplayer.files.tape;

import kvv.aplayer.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

@SuppressLint("NewApi")
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
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeGlobalOnLayoutListener(this);

				BobbinView b1 = (BobbinView) findViewById(R.id.leftBobbin);
				BobbinView b2 = (BobbinView) findViewById(R.id.rightBobbin);

				TapeView tapeView = (TapeView) findViewById(R.id.tape);
				tapeView.setBobbins(b1, b2);
			}
		});
	}

}
