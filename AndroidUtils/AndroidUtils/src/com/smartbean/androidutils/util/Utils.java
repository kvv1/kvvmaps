package com.smartbean.androidutils.util;

import android.util.Log;

public class Utils {
	public static void log(Object obj, String message) {
		String className = obj == null ? "" : obj.getClass().getSimpleName();
		Log.w(className, message);
	}
}
