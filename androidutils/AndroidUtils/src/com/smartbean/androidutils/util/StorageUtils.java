package com.smartbean.androidutils.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

public class StorageUtils {

	public static class StorageInfo {

		public final String path;
		public final boolean readonly;
		public final boolean removable;
		public final int number;

		StorageInfo(String path, boolean readonly, boolean removable, int number) {
			this.path = path;
			this.readonly = readonly;
			this.removable = removable;
			this.number = number;
		}

		public String getDisplayName() {
			StringBuilder res = new StringBuilder();
			if (!removable) {
				res.append("Internal SD card");
			} else if (number > 1) {
				res.append("SD card " + number);
			} else {
				res.append("SD card");
			}
			if (readonly) {
				res.append(" (Read only)");
			}
			return res.toString();
		}
	}

	public static List<StorageInfo> getStorageList() {

		List<StorageInfo> list = new ArrayList<StorageInfo>();
		String def_path = Environment.getExternalStorageDirectory().getPath();
		boolean def_path_removable = Environment.isExternalStorageRemovable();
		String def_path_state = Environment.getExternalStorageState();
		boolean def_path_available = def_path_state
				.equals(Environment.MEDIA_MOUNTED)
				|| def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		boolean def_path_readonly = Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED_READ_ONLY);

		HashSet<String> paths = new HashSet<String>();
		int cur_removable_number = 1;

		if (def_path_available) {
			paths.add(def_path);
			list.add(0, new StorageInfo(def_path, def_path_readonly,
					def_path_removable,
					def_path_removable ? cur_removable_number++ : -1));
		}

		BufferedReader buf_reader = null;
		try {
			buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
			String line;
			// Log.d(TAG, "/proc/mounts");
			while ((line = buf_reader.readLine()) != null) {
				// Log.d(TAG, line);
				if (line.contains("vfat") || line.contains("/mnt")) {
					StringTokenizer tokens = new StringTokenizer(line, " ");
					String unused = tokens.nextToken(); // device
					String mount_point = tokens.nextToken(); // mount point
					if (paths.contains(mount_point)) {
						continue;
					}
					unused = tokens.nextToken(); // file system
					List<String> flags = Arrays.asList(tokens.nextToken()
							.split(",")); // flags
					boolean readonly = flags.contains("ro");

					if (line.contains("/dev/block/vold")) {
						if (!line.contains("/mnt/secure")
								&& !line.contains("/mnt/asec")
								&& !line.contains("/mnt/obb")
								&& !line.contains("/dev/mapper")
								&& !line.contains("tmpfs")) {
							paths.add(mount_point);
							list.add(new StorageInfo(mount_point, readonly,
									true, cur_removable_number++));
						}
					}
				}
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (buf_reader != null) {
				try {
					buf_reader.close();
				} catch (IOException ex) {
				}
			}
		}
		return list;
	}

	private static final Pattern DIR_SEPORATOR = Pattern.compile("/");

	/**
	 * Raturns all available SD-Cards in the system (include emulated)
	 * 
	 * Warning: Hack! Based on Android source code of version 4.3 (API 18)
	 * Because there is no standart way to get it. TODO: Test on future Android
	 * versions 4.4+
	 * 
	 * @return paths to all available SD-Cards in the system (include emulated)
	 */
	public static String[] getStorageDirectories() {
		// Final set of paths
		final Set<String> rv = new HashSet<String>();
		// Primary physical SD-CARD (not emulated)
		final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
		// All Secondary SD-CARDs (all exclude primary) separated by ":"
		final String rawSecondaryStoragesStr = System
				.getenv("SECONDARY_STORAGE");
		// Primary emulated SD-CARD
		final String rawEmulatedStorageTarget = System
				.getenv("EMULATED_STORAGE_TARGET");
		if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
			// Device has physical external storage; use plain paths.
			if (TextUtils.isEmpty(rawExternalStorage)) {
				// EXTERNAL_STORAGE undefined; falling back to default.
				rv.add("/storage/sdcard0");
			} else {
				rv.add(rawExternalStorage);
			}
		} else {
			// Device has emulated storage; external storage paths should have
			// userId burned into them.
			final String rawUserId;
			if (Build.VERSION.SDK_INT < 17/*Build.VERSION_CODES.JELLY_BEAN_MR1*/) {
				rawUserId = "";
			} else {
				final String path = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				final String[] folders = DIR_SEPORATOR.split(path);
				final String lastFolder = folders[folders.length - 1];
				boolean isDigit = false;
				try {
					Integer.valueOf(lastFolder);
					isDigit = true;
				} catch (NumberFormatException ignored) {
				}
				rawUserId = isDigit ? lastFolder : "";
			}
			// /storage/emulated/0[1,2,...]
			if (TextUtils.isEmpty(rawUserId)) {
				rv.add(rawEmulatedStorageTarget);
			} else {
				rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
			}
		}
		// Add all secondary storages
		if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
			// All Secondary SD-CARDs splited into array
			final String[] rawSecondaryStorages = rawSecondaryStoragesStr
					.split(File.pathSeparator);
			Collections.addAll(rv, rawSecondaryStorages);
		}
		return rv.toArray(new String[rv.size()]);
	}
}