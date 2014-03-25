package com.smartbean.androidutils.util;

import android.view.View;
import android.view.ViewGroup;

public class Drawables {
	public static void unbindDrawables(View view) {
		if (view == null)
			return;

		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			try {
				((ViewGroup) view).removeAllViews();
			} catch (UnsupportedOperationException mayHappen) {
				// AdapterViews, ListViews and potentially other ViewGroups
				// don’t support the removeAllViews operation
			}
		}
	}

}
