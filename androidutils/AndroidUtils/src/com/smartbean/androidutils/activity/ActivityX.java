package com.smartbean.androidutils.activity;

import android.app.Activity;

public class ActivityX extends Activity{
	protected void onBG() {
	}

	protected void onFG() {
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus)
			onFG();
		else
			onBG();

		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onPause() {
		onBG();
		super.onPause();
		System.gc();
	}
}
