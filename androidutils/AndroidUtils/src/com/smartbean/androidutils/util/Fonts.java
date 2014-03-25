package com.smartbean.androidutils.util;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Typeface;

public class Fonts {
	private static HashMap<String, Typeface> fontCache = new HashMap<String, Typeface>();

	public static Typeface get(String name, Context context) {
		Typeface tf = fontCache.get(name);
		if (tf == null) {
			try {
				tf = Typeface.createFromAsset(context.getAssets(), name);
			} catch (Exception e) {
				return null;
			}
			fontCache.put(name, tf);
		}
		return tf;
	}

}
