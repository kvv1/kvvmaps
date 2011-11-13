package kvv.kvvmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.Comparator;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.view.CommonDoc;
import kvv.kvvmap.dlg.PathDlg;
import kvv.kvvmap.dlg.PlaceMarkDlg;
import kvv.kvvmap.service.KvvMapsService;
import kvv.kvvmap.service.KvvMapsService.IKvvMapsService;
import kvv.kvvmap.service.Tracker.TrackerListener;
import kvv.kvvmap.view.MapView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

@SuppressWarnings("deprecation")
public class MyActivity extends Activity {

	private final String VERSION = "version: 3.1.0";

	public static final String PREFS_NAME = "KvvMapPrefsFile";

	private static final int MENU_LOGGING_ONOFF = 100;
	private static final int MENU_FOLLOW_ONOFF = 101;
	private static final int MENU_QUIT = 102;
	private static final int MENU_CURRENT_POS = 103;
	private static final int MENU_ADD_POINT = 104;
	private static final int MENU_ADD_PLACEMARK = 105;
	private static final int MENU_ENLARGE = 106;

	private static final int MENU_TRACKS = 108;

	private static final int MENU_DEBUG_DRAW = 110;
	private static final int MENU_ABOUT = 111;
	private static final int MENU_TOGGLE_BUTTONS = 112;

	private MapView view;
	private LocationManager curPosLocationManager;

	// public static MediaPlayer mediaPlayer;

	public Bitmap bmMultimap;
	public Bitmap bmFollow;
	public Bitmap bmWriting;
	public Bitmap bmSendLoc;

	private boolean buttonsVisible;
	public boolean enlarge;

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		int cnt = event.getRepeatCount();
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && cnt == 0) {
			view.reorderMaps();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Adapter.log("MyActivity.onKeyDown");
		/*
		 * if (keyCode == KeyEvent.KEYCODE_BACK) { moveTaskToBack(true); return
		 * true; } else
		 */if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (view != null)
				view.decInfoLevel();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (view != null)
				view.incInfoLevel();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			zoomIn();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			zoomOut();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (event.getRepeatCount() == 0) {
			} else {
				if (view != null && view.getSel() != null)
					editSel();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		Log.w("KVVMAPS", "onPause");
		wakeLock.release();

		((SensorManager) getSystemService(Context.SENSOR_SERVICE))
				.unregisterListener(sensorListener);

		super.onPause();
		System.gc();
	}

	@Override
	protected void onResume() {
		Log.w("KVVMAPS", "onResume");
		if (sensorListener == null) {
			sensorListener = new SensorListener() {
				public void onSensorChanged(int sensor, float[] values) {
					if (view != null)
						view.setCompass(values);
				}

				public void onAccuracyChanged(int sensor, int accuracy) {
				}
			};

		}

		((SensorManager) getSystemService(Context.SENSOR_SERVICE))
				.registerListener(sensorListener,
						SensorManager.SENSOR_ORIENTATION,
						SensorManager.SENSOR_DELAY_NORMAL);

		if (wakeLock == null)
			wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
					.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");

		wakeLock.acquire();
		super.onResume();
	}

	private PowerManager.WakeLock wakeLock;
	private SensorListener sensorListener;

	public IKvvMapsService mapsService;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.w("KVVMAPS", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	private final TrackerListener tl = new TrackerListener() {
		@Override
		public void setMyLocation(LocationX locationX, boolean forceScroll) {
			if (view != null)
				view.setMyLocation(locationX, forceScroll);
		}

		@Override
		public void dimmMyLocation() {
			if (view != null)
				view.dimmMyLocation();
		}
	};

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (view != null) {
				mapsService = (IKvvMapsService) service;
				mapsService.setTrackerListener(tl);
				view.init(MyActivity.this);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			disconnectFromService();
		}
	};

	private void disconnectFromService() {
		if (mapsService != null) {
			mapsService.setTrackerListener(null);
			mapsService = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.w("KVVMAPS", "onCreate");
		super.onCreate(savedInstanceState);

		if (!checkMaps())
			return;

		startService(new Intent(this, KvvMapsService.class));

		bindService(new Intent(this, KvvMapsService.class), conn,
				Context.BIND_AUTO_CREATE);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		enlarge = settings.getBoolean("enlarge", false);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		if (metrics.xdpi > 160)
			Adapter.TILE_SIZE = (int) (Adapter.TILE_SIZE * metrics.xdpi / 145);

		if (enlarge)
			Adapter.TILE_SIZE = Adapter.TILE_SIZE * 13 / 10;

		// Adapter.TILE_SIZE = (int) (Adapter.TILE_SIZE * metrics.xdpi / 100);

		int cachesz = (metrics.widthPixels / Adapter.TILE_SIZE + 3)
				* (metrics.heightPixels / Adapter.TILE_SIZE + 3);
		Adapter.MAP_TILES_CACHE_SIZE = cachesz;
		Adapter.PATH_TILES_CACHE_SIZE = cachesz;

		Adapter.RAF_CACHE_SIZE = cachesz * 2;
		// Adapter.RAF_CACHE_SIZE = 1;

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			private UncaughtExceptionHandler defaultUEH = Thread
					.getDefaultUncaughtExceptionHandler();

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				PrintStream errPrintStream;
				try {
					OutputStream errOS = new FileOutputStream(Adapter.ROOT
							+ "/" + System.currentTimeMillis() + ".log");
					errPrintStream = new PrintStream(errOS, true);
					ex.printStackTrace(errPrintStream);
					errOS.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				defaultUEH.uncaughtException(thread, ex);
			}
		});

		CommonDoc.debugDraw = settings.getBoolean("debugDraw", false);

		bmMultimap = BitmapFactory.decodeResource(getResources(),
				R.drawable.multimaps);
		bmFollow = BitmapFactory.decodeResource(getResources(),
				R.drawable.follow);
		bmWriting = BitmapFactory.decodeResource(getResources(),
				R.drawable.writing);
		bmSendLoc = BitmapFactory.decodeResource(getResources(),
				R.drawable.sendloc);

		setContentView(R.layout.screen);

		view = (MapView) findViewById(R.id.MapView);

		ImageButton button = (ImageButton) findViewById(R.id.edit);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editSel();
			}
		});

		button = (ImageButton) findViewById(R.id.infoplus);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.incInfoLevel();
			}
		});

		button = (ImageButton) findViewById(R.id.infominus);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.decInfoLevel();
			}
		});

		button = (ImageButton) findViewById(R.id.zoomin);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomIn();
			}
		});

		button = (ImageButton) findViewById(R.id.zoomout);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomOut();
			}
		});

		button = (ImageButton) findViewById(R.id.rotate);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.reorderMaps();
			}
		});

		buttonsVisible = settings.getBoolean("buttonsVisible", true);
		updateButtons();
	}

	private boolean checkMaps() {
		if (new File(Adapter.MAPS_ROOT + "/default.dir").exists()
				&& new File(Adapter.MAPS_ROOT + "/default.pac").exists())
			return true;
		if (new File(Adapter.MAPS_ROOT + "/land.dir").exists()
				&& new File(Adapter.MAPS_ROOT + "/land.pac").exists())
			return true;

		try {

			if (!new File(Adapter.MAPS_ROOT).exists()) {
				new File(Adapter.MAPS_ROOT).mkdirs();
			}

			if (!new File(Adapter.PATH_ROOT).exists()) {
				new File(Adapter.PATH_ROOT).mkdirs();
			}

			if (!new File(Adapter.PLACEMARKS).exists()) {
				new File(Adapter.PLACEMARKS).createNewFile();
			}
		} catch (IOException e) {
			new AlertDialog.Builder(MyActivity.this)
					.setMessage("��� �������� ������")
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					}).show();
			return false;
		}

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					new MapLoader(MyActivity.this).load();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					finish();
					break;
				}
			}
		};

		new AlertDialog.Builder(this)
				.setMessage(
						"�� ������� �������� �����.\n��������� �������� ����� (500Kb)?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();

		return false;
	}

	private void updateButtons() {
		View buttons = findViewById(R.id.linearLayout1);
		if (buttonsVisible)
			buttons.setVisibility(View.VISIBLE);
		else
			buttons.setVisibility(View.GONE);
	}

	private void zoomIn() {
		view.zoomIn();
	}

	private void zoomOut() {
		view.zoomOut();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_LOGGING_ONOFF).setTitle(
				mapsService.getTracker().isTracking() ? "������ ���� ����."
						: "������ ����");
		menu.findItem(MENU_FOLLOW_ONOFF)
				.setTitle(
						mapsService.getTracker().isFollowing() ? "�������� �� GPS ����."
								: "�������� �� GPS");
		menu.findItem(MENU_DEBUG_DRAW).setTitle(
				CommonDoc.debugDraw ? "Debug drawing off" : "Debug drawing on");
		menu.findItem(MENU_ENLARGE).setTitle(
				enlarge ? "���������� ������" : "����������� ������");
		menu.findItem(MENU_TOGGLE_BUTTONS).setTitle(
				buttonsVisible ? "�������� ������ ����."
						: "�������� ������ ���.");
		return super.onPrepareOptionsMenu(menu);
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.addSubMenu(0, MENU_PATHS, order, title)
		menu.add(0, MENU_CURRENT_POS, 0, "�����");
		menu.add(0, MENU_LOGGING_ONOFF, 0, "GPS On/Off");
		menu.add(0, MENU_FOLLOW_ONOFF, 0, "GPS On/Off");
		// menu.add(0, MENU_ADD_POINT, 0, "Add point");
		menu.add(0, MENU_ADD_PLACEMARK, 0, "�������� �����");
		// menu.add(0, MENU_NEW_PATH, 0, "New path");
		menu.add(0, MENU_TRACKS, 0, "����");
		menu.add(0, MENU_ENLARGE, 0, "����������� ������");
		menu.add(0, MENU_DEBUG_DRAW, 0, "debugDraw");
		menu.add(0, MENU_TOGGLE_BUTTONS, 0, "�������� ������");
		menu.add(0, MENU_ABOUT, 0, "� ���������");
		menu.add(0, MENU_QUIT, 0, "�����");
		return true;
	}

	private AlertDialog curLocProgress;

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CURRENT_POS:
			gotoCurrentPos();
			return true;
		case MENU_ADD_POINT:
			addPoint();
			return true;
		case MENU_LOGGING_ONOFF:
			trackingOnOff();
			return true;
		case MENU_FOLLOW_ONOFF:
			followOnOff();
			return true;
		case MENU_ADD_PLACEMARK:
			addPlacemark();
			return true;
		case MENU_TRACKS:
			paths();
			return true;
		case MENU_DEBUG_DRAW:
			debugDrawOnOff();
			return true;
		case MENU_TOGGLE_BUTTONS:
			buttonsOnOff();
			return true;
		case MENU_ENLARGE:
			enlargeOnOff();
			return true;
		case MENU_ABOUT:
			about();
			return true;
		case MENU_QUIT:
			stopService(new Intent(this, KvvMapsService.class));
			finish();
			return true;
		default:
		}
		return false;
	}

	private void buttonsOnOff() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Editor prefsPrivateEditor = settings.edit();
		buttonsVisible = !buttonsVisible;
		prefsPrivateEditor.putBoolean("buttonsVisible", buttonsVisible);
		prefsPrivateEditor.commit();
		updateButtons();
	}

	private void enlargeOnOff() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Editor prefsPrivateEditor = settings.edit();
		enlarge = !enlarge;
		prefsPrivateEditor.putBoolean("enlarge", enlarge);
		prefsPrivateEditor.commit();
		new AlertDialog.Builder(this)
				.setMessage(
						"��� ���������� ��������� � ���� ���������� ������������� ���������.")
				.show();
	}

	private void about() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("About");
		alertDialog.setMessage("KvvMaps\n" + VERSION
				+ "\nVladimir Krupsky\nv_krupsky@mail.ru");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alertDialog.show();
	}

	private void debugDrawOnOff() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		Editor prefsPrivateEditor = settings.edit();
		CommonDoc.debugDraw = !CommonDoc.debugDraw;
		prefsPrivateEditor.putBoolean("debugDraw", CommonDoc.debugDraw);
		prefsPrivateEditor.commit();
		view.clearPathTiles();
	}

	private void paths() {
		final Path[] paths = view.getPaths().getPaths().toArray(new Path[0]);

		Arrays.sort(paths, new Comparator<Path>() {
			public int compare(Path path1, Path path2) {
				LocationX pm1 = path1.getNearest(view.getCenter());
				LocationX pm2 = path2.getNearest(view.getCenter());
				if (pm1 == null)
					return 1;
				if (pm2 == null)
					return -1;

				double d1 = pm1.distanceTo(view.getCenter());
				double d2 = pm2.distanceTo(view.getCenter());
				if (d1 < d2)
					return -1;
				if (d1 > d2)
					return 1;
				return 0;
			}
		});

		final String[] names = new String[paths.length];
		for (int i = 0; i < paths.length; i++)
			names[i] = paths[i].getName();

		final boolean[] checks = new boolean[paths.length];
		for (int i = 0; i < paths.length; i++)
			checks[i] = paths[i].isEnabled();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Paths");
		builder.setMultiChoiceItems(names, checks,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						checks[which] = isChecked;
						paths[which].setEnabled(isChecked);
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void followOnOff() {
		if (mapsService.getTracker().isFollowing())
			mapsService.getTracker().stopFollow();
		else {
			mapsService.getTracker().startFollow();
			gotoCurrentPos();
		}
		updateView();
	}

	private void gotoCurrentPos() {
		final LocationListener locListener = new LocationListener() {
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				if (status == LocationProvider.OUT_OF_SERVICE) {
					if (curPosLocationManager != null) {
						curPosLocationManager.removeUpdates(this);
						curPosLocationManager = null;
					}
				}
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
				if (curPosLocationManager != null) {
					curPosLocationManager.removeUpdates(this);
					curPosLocationManager = null;
				}
			}

			public void onLocationChanged(Location location) {
				if (location != null) {
					if (location.getAccuracy() < 40) {
						if (curPosLocationManager != null) {
							curPosLocationManager.removeUpdates(this);
						}
						if (curLocProgress != null)
							curLocProgress.dismiss();
						curLocProgress = null;
					}
					LocationX loc = new LocationX(location);
					tl.setMyLocation(loc, true);
				}
			}
		};

		curPosLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		curPosLocationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 1, 1, locListener);

		curLocProgress = ProgressDialog.show(this, "",
				"����������� ���������...", true, true, new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						if (curPosLocationManager != null) {
							curPosLocationManager.removeUpdates(locListener);
							curPosLocationManager = null;
						}
						curLocProgress = null;
					}
				});
	}

	private void addPoint() {
		Location loc = curPosLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (loc != null && view != null) {
			// try {
			// view.addPoint(loc);
			// } catch (IOException e) {
			// Toast.makeText(getApplicationContext(), "cannot add point",
			// Toast.LENGTH_SHORT).show();
			// }
		}
	}

	private void addPlacemark() {
		final LocationX pm = view.createPlacemark();
		if (pm != null) {
			PlaceMarkDlg dlg = new PlaceMarkDlg(this, view, pm,
					view.getPlacemarks(), PlaceMarkDlg.Type.ADD);
			dlg.show();
		}
	}

	private void editSel() {
		if (view == null)
			return;

		ISelectable sel = view.getSel();
		if (sel == null)
			return;

		if (sel instanceof LocationX) {
			LocationX pm = (LocationX) sel;
			PlaceMarkDlg dlg = new PlaceMarkDlg(this, view, pm,
					view.getPlacemarks(), PlaceMarkDlg.Type.EDIT);
			dlg.show();
		}

		if (sel instanceof PathSelection) {
			PathSelection pathSel = (PathSelection) sel;
			new PathDlg(this, pathSel.path, pathSel.pm, view.getPaths()).show();
		}
	}

	private void trackingOnOff() {
		if (mapsService.getTracker().isTracking()) {
			mapsService.getTracker().endPath();
		} else {
			mapsService.getTracker().startPath();
		}
		updateView();
	}

	@Override
	protected void onDestroy() {
		if (view != null)
			view.save();
		Adapter.log("onDestroy");
		unbindService(conn);
		disconnectFromService();
		if (view != null)
			view.dispose();
		view = null;
		super.onDestroy();
	}

	public void updateView() {
		if (view != null)
			view.invalidate();
	}

	public void animateTo(LocationX location) {
		if (view != null)
			view.animateTo(location);
	}

}