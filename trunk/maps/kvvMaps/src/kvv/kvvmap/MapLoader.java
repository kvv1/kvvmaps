package kvv.kvvmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import kvv.kvvmap.adapter.Adapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Environment;

public class MapLoader {
	public static final String DEFAULT_MAP_DIR_NAME = "default.dir";
	public static final String DEFAULT_MAP_PAC_NAME = "default.pac";
	protected static final String DEFAULT_MAP_URL_PAC = "http://palermo.ru/vladimir/kvvMaps/" + DEFAULT_MAP_PAC_NAME;
	protected static final String DEFAULT_MAP_URL_DIR = "http://palermo.ru/vladimir/kvvMaps/" + DEFAULT_MAP_DIR_NAME;

	private final Activity activity;

	public MapLoader(Activity activity) {
		this.activity = activity;
	}

	public void load() {
		ProgressDialog.show(activity, "", "Загрузка обзорной карты...", true, true,
				new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mapLoader.interrupt();
					}
				});

		mapLoader.start();
	}

	private Thread mapLoader = new Thread() {
		@Override
		public void run() {
			BufferedInputStream is = null;
			BufferedOutputStream os = null;
			try {
				byte[] buf = new byte[4096];

				is = new BufferedInputStream(
						new URL(DEFAULT_MAP_URL_DIR).openStream());
				os = new BufferedOutputStream(new FileOutputStream(
						Adapter.MAPS_ROOT + "/default.dir_"));

				int n;
				while ((n = is.read(buf)) != -1) {
					if (interrupted())
						throw new InterruptedException();
					os.write(buf, 0, n);
				}

				is.close();
				os.close();
				is = new BufferedInputStream(
						new URL(DEFAULT_MAP_URL_PAC).openStream());
				os = new BufferedOutputStream(new FileOutputStream(
						Adapter.MAPS_ROOT + "/default.pac_"));

				while ((n = is.read(buf)) != -1) {
					//System.out.println("-- " + n);
					if (interrupted())
						throw new InterruptedException();
					os.write(buf, 0, n);
				}

				is.close();
				os.close();
				new File(Adapter.MAPS_ROOT + "/default.pac_")
						.renameTo(new File(Adapter.MAPS_ROOT + "/default.pac"));
				new File(Adapter.MAPS_ROOT + "/default.dir_")
						.renameTo(new File(Adapter.MAPS_ROOT + "/default.dir"));
			} catch (Exception e) {
				e.printStackTrace();
				activity.runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(activity)
								.setMessage("Ошибка загрузки обзорной карты.")
								.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										activity.finish();
									}
								}).show();
					}
				});
				return;
			} finally {
				new File(Adapter.MAPS_ROOT + "/default.dir_").delete();
				new File(Adapter.MAPS_ROOT + "/default.pac_").delete();
			}

			activity.runOnUiThread(new Runnable() {
				public void run() {
					new AlertDialog.Builder(activity)
							.setMessage(
									"Обзорная карта загружена.\nЗапустите программу еще раз.")
							.setOnCancelListener(new OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									activity.finish();
								}
							}).show();
				}
			});
		}
	};
	
	public static boolean checkMaps(final Activity activity) {
		
		try {
			if (!new File(Adapter.ROOT_INT).exists())
				new File(Adapter.ROOT_INT).mkdirs();

			if (!new File(Adapter.MAPS_ROOT).exists())
				new File(Adapter.MAPS_ROOT).mkdirs();

			if (!new File(Adapter.PATH_ROOT).exists()) 
				new File(Adapter.PATH_ROOT).mkdirs();

			if (!new File(Adapter.PLACEMARKS).exists()) 
				new File(Adapter.PLACEMARKS).createNewFile();
		} catch (IOException e) {
			new AlertDialog.Builder(activity)
					.setMessage("Нет карточки памяти")
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							activity.finish();
						}
					}).show();
			return false;
		}

		if (new File(Adapter.MAPS_ROOT + "/default.dir").exists()
				&& new File(Adapter.MAPS_ROOT + "/default.pac").exists())
			return true;
		if (new File(Adapter.MAPS_ROOT + "/land.dir").exists()
				&& new File(Adapter.MAPS_ROOT + "/land.pac").exists())
			return true;

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					new MapLoader(activity).load();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					activity.finish();
					break;
				}
			}
		};

		new AlertDialog.Builder(activity)
				.setMessage(
						"Не найдено обзорной карты.\nЗагрузить обзорную карту (500Kb)?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();

		return false;
	}


}
