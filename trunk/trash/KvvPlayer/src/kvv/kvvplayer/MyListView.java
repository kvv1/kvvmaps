package kvv.kvvplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.ListView;

public class MyListView extends ListView {

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	
	
	
}
