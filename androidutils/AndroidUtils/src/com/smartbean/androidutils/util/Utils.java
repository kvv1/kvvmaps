package com.smartbean.androidutils.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Debug;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

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
			List<String> choices, final AsyncCallback<Integer> callback) {
		select(context, title, choices.toArray(new String[0]), callback);
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

	public static void enterNumber(Context context, String title,
			final AsyncCallback<Integer> callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);

		final EditText input = new EditText(context);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(input);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int cnt = Integer.parseInt(input.getText().toString());
				callback.onSuccess(cnt);
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

	public static void playDefaultNotificationSound(Context context) {
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone r = RingtoneManager.getRingtone(context, notification);
		r.play();
	}

	public static void printMem() {
		long freeSize = 0L;
		long totalSize = 0L;
		long usedSize = -1L;
		try {
			Runtime.getRuntime().gc();

			Runtime info = Runtime.getRuntime();
			freeSize = info.freeMemory();
			totalSize = info.totalMemory();
			usedSize = totalSize - freeSize;

			long usedMegs = Debug.getNativeHeapAllocatedSize();
			long freeMegs = Debug.getNativeHeapFreeSize();
			long allMegs = Debug.getNativeHeapSize();

			// long div = 1048576L;
			long div = 1024;

			System.out.println(" TOTAL USED FREE");
			System.out.println("J: " + totalSize / div + " " + usedSize / div
					+ " " + freeSize / div + " ");
			System.out.println("N: " + allMegs / div + " " + usedMegs / div
					+ " " + freeMegs / div + " ");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("DefaultLocale")
	public static String convertSecondsToHMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;

		if (h != 0)
			return String.format("%d:%02d:%02d", h, m, s);
		return String.format("%02d:%02d", m, s);
	}


}
