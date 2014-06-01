package com.smartbean.androidutils.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

public class Utils {
	public static void log(Object obj, String message) {
		String className = obj == null ? "" : obj.getClass().getSimpleName();
		Log.w(className, message);
	}

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString
						.append(String.format("%02X", 0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			log(null, e.getMessage());
			e.printStackTrace();
		}
		return "";
	}

	public interface ToString<T> {
		String toString(T t);
	}

	public static <T> String[] toStringArray(T[] arr, ToString<T> toString) {
		return toStringArray(Arrays.asList(arr), toString);
	}
	
	public static <T> String[] toStringArray(List<T> list, ToString<T> toString) {
		String[] res = new String[list.size()];
		int i = 0;
		for (T t : list)
			res[i++] = toString == null ? t.toString() : toString.toString(t);
		return res;
	}

	public static void select(Context context, String title,
			final String[] choices, final AsyncCallback<Integer> callback) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setItems(choices, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onSuccess(which);
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				callback.onFailure();
			}
		});
		builder.show();
	}

}
