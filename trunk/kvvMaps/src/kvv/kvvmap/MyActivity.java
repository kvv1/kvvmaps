package kvv.kvvmap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;

import kvv.kvvmap.adapter.Adapter;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.COLOR;
import kvv.kvvmap.common.Pair;
import kvv.kvvmap.common.maps.Maps;
import kvv.kvvmap.common.maps.MapsDir;
import kvv.kvvmap.common.pacemark.ISelectable;
import kvv.kvvmap.common.pacemark.Path;
import kvv.kvvmap.common.pacemark.PathSelection;
import kvv.kvvmap.common.view.CommonView.RotationMode;
import kvv.kvvmap.common.view.Environment;
import kvv.kvvmap.dlg.PathDlg;
import kvv.kvvmap.dlg.PlaceMarkDlg;
import kvv.kvvmap.service.KvvMapsService;
import kvv.kvvmap.service.KvvMapsService.IKvvMapsService;
import kvv.kvvmap.service.KvvMapsService.KvvMapsServiceListener;
import kvv.kvvmap.view.DiagramView;
import kvv.kvvmap.view.MapView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

@SuppressWarnings("deprecation")
public class MyActivity extends Activity {

	private static final String BUTTONS_VISIBLE_SETTING = "buttonsVisible";
	private static final String FOLLOW_GPS_SETTING = "followGPS";
	private static final String KINETIC_SCROLLING_SETTING = "kineticScrolling";
	private static final String LOAD_DURING_SCROLLING_SETTING = "loadDuringScrolling";

	public static final String PREFS_NAME = "KvvMapPrefsFile";

	private static final int MENU_LOGGING_ONOFF = 100;
	private static final int MENU_QUIT = 102;
	private static final int MENU_ADD_PLACEMARK = 105;
	private static final int MENU_KINETIC_SCROLLING = 106;
	private static final int MENU_LOAD_DURING_SCROLLING = 107;

	private static final int MENU_TRACKS = 108;

	private static final int MENU_ROTATION_GROUP = 109;
	private static final int MENU_ROTATION_NONE = MENU_ROTATION_GROUP * 10 + 1;
	private static final int MENU_ROTATION_COMPASS = MENU_ROTATION_GROUP * 10 + 2;
	private static final int MENU_ROTATION_GPS = MENU_ROTATION_GROUP * 10 + 3;

	private static final int MENU_DEBUG_DRAW = 110;
	private static final int MENU_ABOUT = 111;
	private static final int MENU_TOGGLE_BUTTONS = 112;

	private static final int MENU_UPDATE = 113;
	// private static final int MENU_UPDATE1 = 114;

	private MapView view;
	private DiagramView diagramView;
	private TextView altSpeed;

	// public static MediaPlayer mediaPlayer;

	private Adapter adapter;
	private PowerManager.WakeLock wakeLock;
	private SensorListener sensorListener;
	private SharedPreferences settings;
	public IKvvMapsService mapsService;

	private final Handler handler = new Handler();

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

	private Runnable gpsOff = new Runnable() {
		@Override
		public void run() {
			stopGPS();
		}
	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		Adapter.log("onWindowFocusChanged " + hasFocus);

		if (hasFocus) {
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
						.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
								"kvvMaps wake lock");

			wakeLock.acquire();

			handler.removeCallbacks(gpsOff);
			if (following())
				startGPS(false);
			updateGpsButton();
			updateWritingButton();

		} else {
			if (wakeLock != null)
				wakeLock.release();

			((SensorManager) getSystemService(Context.SENSOR_SERVICE))
					.unregisterListener(sensorListener);

			handler.postDelayed(gpsOff, 120000);
		}

		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onPause() {
		Log.w("KVVMAPS", "onPause");

		super.onPause();
		System.gc();
	}

	@Override
	protected void onResume() {
		Log.w("KVVMAPS", "onResume");
		Adapter.log("onResume");

		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.w("KVVMAPS", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (view != null) {
				Adapter.log("onServiceConnected " + view + " "
						+ MyActivity.this);
				mapsService = (IKvvMapsService) service;

				MapsDir mapsDir = mapsService.getMapsDir();
				Environment envir = new Environment(adapter,
						mapsService.getPaths(), mapsService.getPlacemarks(),
						new Maps(adapter, mapsDir), mapsDir);

				Bundle b = mapsService.getBundle();
				view.init(MyActivity.this, envir, b, getRotationMode());
				diagramView.init(envir.adapter);

				mapsService.setListener(new KvvMapsServiceListener() {
					@Override
					public void mapsLoaded() {
						if (view != null)
							view.repaint();
					}
				});

				updateWritingButton();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			disconnectFromService();
		}
	};

	private void disconnectFromService() {
		if (mapsService != null) {
			mapsService.disconnect();
			mapsService = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.w("KVVMAPS", "onCreate");
		super.onCreate(savedInstanceState);
		
		settings = getSharedPreferences(PREFS_NAME, 0);

		if (!MapLoader.checkMaps(this))
			return;

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		int tileSz = 256;
		if (metrics.xdpi > 160)
			tileSz = (int) (tileSz * metrics.xdpi / 145);
		Adapter.TILE_SIZE = tileSz;

		int cachesz = (metrics.widthPixels / Adapter.TILE_SIZE + 3)
				* (metrics.heightPixels / Adapter.TILE_SIZE + 3);
		Adapter.MAP_TILES_CACHE_SIZE = cachesz * 2;
		Adapter.RAF_CACHE_SIZE = cachesz * 2;

		adapter = new Adapter(this);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		startService(new Intent(this, KvvMapsService.class));

		bindService(new Intent(this, KvvMapsService.class), conn,
				Context.BIND_AUTO_CREATE);

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

		Adapter.debugDraw = settings.getBoolean("debugDraw", false);

		setContentView(R.layout.screen);
		altSpeed = (TextView) findViewById(R.id.altSpeed);
		altSpeed.setBackgroundColor(0x80000000);
		altSpeed.setTextColor(COLOR.CYAN);
		altSpeed.setText("");

		ImageButton button = (ImageButton) findViewById(R.id.edit);
		button.setAlpha(255);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editSel();
			}
		});

		button = (ImageButton) findViewById(R.id.info);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.incInfoLevel();
			}
		});

		button = (ImageButton) findViewById(R.id.zoomin);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomIn();
			}
		});

		button = (ImageButton) findViewById(R.id.zoomout);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomOut();
			}
		});

		button = (ImageButton) findViewById(R.id.rotate);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.reorderMaps();
			}
		});

//		button = (ImageButton) findViewById(R.id.gps);
//		button.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				gpsOnOff();
//			}
//		});

		button = (ImageButton) findViewById(R.id.fixedmap);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				fixUnfixMap();
			}
		});

		ImageButton hereButton = (ImageButton) findViewById(R.id.here);
		hereButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.animateToMyLocation();
			}
		});

		Button toTarget = (Button) findViewById(R.id.toTarget);
		toTarget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view != null)
					view.animateToTarget();
			}
		});

		toTarget.setBackgroundColor((COLOR.TARG_COLOR & 0x00FFFFFF) | 0x64000000);
		toTarget.setFocusable(false);

		
		
		final ToggleButton gps = (ToggleButton) findViewById(R.id.gps);
		gps.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if(gps.isChecked())
					gps.setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_on));
				else
					gps.setBackgroundDrawable(getResources().getDrawable(R.drawable.gps_off));
			}
		});
		gps.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				System.out.println("XXX " + gps.isChecked());
			}
		});
		
		
		
		
		updateButtons();

		view = (MapView) findViewById(R.id.MapView);
		diagramView = (DiagramView) findViewById(R.id.diagramView);

	}

	void setupImageButton(int buttonId, int img, int imgDisabled) {
		ImageButton button = (ImageButton) findViewById(buttonId);
		button.setBackgroundResource(img);
		button.setImageResource(img);
	}
	
	private boolean buttonsVisible() {
		return settings.getBoolean(BUTTONS_VISIBLE_SETTING, true);
	}

	private boolean following() {
		return settings.getBoolean(FOLLOW_GPS_SETTING, true);
	}

	private void updateButtons() {
		View buttons = findViewById(R.id.screenButtons);
		if (buttonsVisible())
			buttons.setVisibility(View.VISIBLE);
		else
			buttons.setVisibility(View.GONE);
	}

	private void zoomIn() {
		if (view != null)
			view.zoomIn();
	}

	private void zoomOut() {
		if (view != null)
			view.zoomOut();
	}

	private RotationMode getRotationMode() {
		return mapsService == null ? RotationMode.ROTATION_NONE : RotationMode
				.values()[mapsService.getBundle().getInt("rotation", 0)];
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.findItem(MENU_LOGGING_ONOFF).setChecked(
				mapsService != null && mapsService.getTracker().isTracking());
		if (mapsService != null && mapsService.getTracker().isTracking())
			menu.findItem(MENU_LOGGING_ONOFF).setTitle("������ ���� ����");
		else
			menu.findItem(MENU_LOGGING_ONOFF).setTitle("������ ����");

		menu.findItem(MENU_DEBUG_DRAW).setChecked(Adapter.debugDraw);
		menu.findItem(MENU_TOGGLE_BUTTONS).setChecked(buttonsVisible());
		menu.findItem(MENU_KINETIC_SCROLLING).setChecked(
				settings.getBoolean(KINETIC_SCROLLING_SETTING, true));
		menu.findItem(MENU_LOAD_DURING_SCROLLING).setChecked(
				settings.getBoolean(LOAD_DURING_SCROLLING_SETTING, true));

		RotationMode rot = getRotationMode();

		if (rot == RotationMode.ROTATION_NONE)
			menu.findItem(MENU_ROTATION_NONE).setChecked(true);
		if (rot == RotationMode.ROTATION_COMPASS)
			menu.findItem(MENU_ROTATION_COMPASS).setChecked(true);
		if (rot == RotationMode.ROTATION_GPS)
			menu.findItem(MENU_ROTATION_GPS).setChecked(true);

		return super.onPrepareOptionsMenu(menu);
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.addSubMenu(0, MENU_PATHS, order, title)

		menu.add(Menu.NONE, MENU_LOGGING_ONOFF, 0, "������ ����").setCheckable(
				true);
		menu.add(Menu.NONE, MENU_ADD_PLACEMARK, 0, "�������� �����");
		SubMenu rotationSubMenu = menu.addSubMenu("�������� �����");
		menu.add(Menu.NONE, MENU_TRACKS, 0, "����");

		SubMenu settingsSubMenu = menu.addSubMenu("���������");

		settingsSubMenu.add(Menu.NONE, MENU_DEBUG_DRAW, 0, "debug info")
				.setCheckable(true);
		settingsSubMenu.add(Menu.NONE, MENU_TOGGLE_BUTTONS, 0,
				"�������� ������").setCheckable(true);
		settingsSubMenu.add(Menu.NONE, MENU_KINETIC_SCROLLING, 0,
				"������� ���������").setCheckable(true);
		settingsSubMenu.add(Menu.NONE, MENU_LOAD_DURING_SCROLLING, 0,
				"���������� ��� ���������").setCheckable(true);
		menu.add(Menu.NONE, MENU_ABOUT, 0, "� ���������");
		menu.add(Menu.NONE, MENU_UPDATE, 0, "Update");
		// menu.add(0, MENU_UPDATE1, 0, "Update test");
		menu.add(Menu.NONE, MENU_QUIT, 0, "�����");

		rotationSubMenu.add(MENU_ROTATION_GROUP, MENU_ROTATION_NONE, 0,
				"��� ��������");
		rotationSubMenu.add(MENU_ROTATION_GROUP, MENU_ROTATION_COMPASS, 0,
				"������");
		rotationSubMenu.add(MENU_ROTATION_GROUP, MENU_ROTATION_GPS, 0, "GPS");
		rotationSubMenu.setGroupCheckable(MENU_ROTATION_GROUP, true, true);

		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LOGGING_ONOFF:
			trackingOnOff();
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
		case MENU_ABOUT:
			about();
			return true;
		case MENU_ROTATION_NONE:
			if (mapsService == null)
				return true;
			mapsService.getBundle().putInt("rotation",
					RotationMode.ROTATION_NONE.ordinal());
			if (view != null)
				view.setRotationMode(getRotationMode());
			return true;
		case MENU_ROTATION_COMPASS:
			if (mapsService == null)
				return true;
			mapsService.getBundle().putInt("rotation",
					RotationMode.ROTATION_COMPASS.ordinal());
			if (view != null)
				view.setRotationMode(getRotationMode());
			return true;
		case MENU_ROTATION_GPS:
			if (mapsService == null)
				return true;
			mapsService.getBundle().putInt("rotation",
					RotationMode.ROTATION_GPS.ordinal());
			if (view != null)
				view.setRotationMode(getRotationMode());
			return true;
		case MENU_KINETIC_SCROLLING: {
			Editor ed = settings.edit();
			ed.putBoolean(KINETIC_SCROLLING_SETTING,
					!settings.getBoolean(KINETIC_SCROLLING_SETTING, true));
			ed.commit();
			return true;
		}
		case MENU_LOAD_DURING_SCROLLING: {
			Editor ed = settings.edit();
			ed.putBoolean(LOAD_DURING_SCROLLING_SETTING,
					!settings.getBoolean(LOAD_DURING_SCROLLING_SETTING, true));
			ed.commit();
			return true;
		}
		case MENU_UPDATE:
			try {
				updateSoftware();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
			// case MENU_UPDATE1:
			// try {
			// updateSoftware1();
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// return true;
		case MENU_QUIT:
			stopGPS();
			stopService(new Intent(this, KvvMapsService.class));
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					System.exit(0);
				}
			}, 1000);
			finish();
			return true;
		default:
		}
		return false;
	}

	private void updateSoftware() throws MalformedURLException, IOException {
		startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://palermo.ru/vladimir/kvvMaps/kvvMaps.apk")));
	}

	// private void updateSoftware1() throws MalformedURLException, IOException
	// {
	// startActivity(new Intent(Intent.ACTION_VIEW,
	// Uri.parse("http://palermo.ru/vladimir/kvvMaps.apk")));
	// }

	private void fixUnfixMap() {
		if (mapsService == null || view == null)
			return;
		String fixedMap = mapsService.getBundle().getString("fixedMap");
		fixedMap = view.fixMap(fixedMap == null);
		mapsService.getBundle().putString("fixedMap", fixedMap);

		ImageButton button = (ImageButton) findViewById(R.id.fixedmap);
		button.setImageBitmap(BitmapFactory.decodeResource(getResources(),
				fixedMap == null ? R.drawable.fixedmap : R.drawable.fixedmapon));

	}

	public String getFixedMap() {
		if (mapsService == null)
			return null;
		return mapsService.getBundle().getString("fixedMap");
	}

	private void buttonsOnOff() {
		Editor prefsPrivateEditor = settings.edit();
		prefsPrivateEditor.putBoolean(BUTTONS_VISIBLE_SETTING,
				!buttonsVisible());
		prefsPrivateEditor.commit();
		updateButtons();
	}

	private void about() {

		try {
			String app_ver = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("About");
			alertDialog.setMessage("KvvMaps\nversion: " + app_ver
					+ "\nVladimir Krupsky\nv_krupsky@mail.ru");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alertDialog.show();
		} catch (NameNotFoundException e) {
		}
	}

	private void debugDrawOnOff() {
		Editor prefsPrivateEditor = settings.edit();
		Adapter.debugDraw = !Adapter.debugDraw;
		prefsPrivateEditor.putBoolean("debugDraw", Adapter.debugDraw);
		prefsPrivateEditor.commit();
		if (view != null)
			view.invalidatePathTiles();
	}

	static class PathLocPair extends Pair<Path, LocationX> {
		public PathLocPair(Path first, LocationX second) {
			super(first, second);
		}
	}

	private void paths() {
		final Path[] paths = mapsService.getPaths().getPaths()
				.toArray(new Path[0]);

		final PathLocPair[] paths1 = new PathLocPair[paths.length];

		for (int i = 0; i < paths.length; i++)
			paths1[i] = new PathLocPair(paths[i], paths[i].getNearest(view
					.getCenter()));

		Arrays.sort(paths1, new Comparator<PathLocPair>() {
			public int compare(PathLocPair path1, PathLocPair path2) {
				LocationX pm1 = path1.second;
				LocationX pm2 = path2.second;
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

		final String[] names = new String[paths1.length];
		for (int i = 0; i < paths1.length; i++)
			names[i] = paths1[i].first.getName();

		final boolean[] checks = new boolean[paths1.length];
		for (int i = 0; i < paths1.length; i++)
			checks[i] = paths1[i].first.isEnabled();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Paths");
		builder.setMultiChoiceItems(names, checks,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						checks[which] = isChecked;
						paths1[which].first.setEnabled(isChecked);
					}
				});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private LocationManager locationManager;
	private LocationListener locationListener;

	public boolean isGPS() {
		return locationListener != null;
	}

	private void startGPS(final boolean fromMenu) {
		if (locationListener == null) {
			locationListener = new LocationListener() {
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					if (status == LocationProvider.OUT_OF_SERVICE) {
						// stopFollow();
					}
				}

				public void onProviderEnabled(String provider) {
				}

				public void onProviderDisabled(String provider) {
					// stopFollow();
				}

				private boolean scroll = fromMenu;

				public void onLocationChanged(Location location) {
					LocationX loc = new LocationX(location);
					if (view != null)
						view.setMyLocation(loc, scroll);
					scroll = false;

					altSpeed.setText("" + (int) loc.getAltitude() + "m "
							+ (int) (loc.getSpeed() * 3.6f) + "km/h");
				}
			};

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1, 1, locationListener);
		}
	}

	private void stopGPS() {
		if (locationListener != null) {
			altSpeed.setText("");
			locationManager.removeUpdates(locationListener);
			locationListener = null;
			if (view != null)
				view.dimmMyLocation();
		}
	}

	private void gpsOnOff() {
		if (!following()) {
			startGPS(true);
			Editor prefsPrivateEditor = settings.edit();
			prefsPrivateEditor.putBoolean(FOLLOW_GPS_SETTING, true);
			prefsPrivateEditor.commit();
		} else {
			stopGPS();
			Editor prefsPrivateEditor = settings.edit();
			prefsPrivateEditor.putBoolean(FOLLOW_GPS_SETTING, false);
			prefsPrivateEditor.commit();
		}
		updateGpsButton();
		updateView();
	}

	private void updateGpsButton() {
//		ImageButton button = (ImageButton) findViewById(R.id.gps);
//		button.setImageBitmap(BitmapFactory.decodeResource(getResources(),
//				following() ? R.drawable.gpson : R.drawable.gps));
	}

	// private void gotoCurrentPos() {
	// final AlertDialog[] curLocProgress = new AlertDialog[1];
	//
	// final LocationListener locListener = new LocationListener() {
	// public void onStatusChanged(String provider, int status,
	// Bundle extras) {
	// if (status == LocationProvider.OUT_OF_SERVICE) {
	// locationManager.removeUpdates(this);
	// if (curLocProgress[0] != null)
	// curLocProgress[0].dismiss();
	// curLocProgress[0] = null;
	// }
	// }
	//
	// public void onProviderEnabled(String provider) {
	// }
	//
	// public void onProviderDisabled(String provider) {
	// locationManager.removeUpdates(this);
	// if (curLocProgress[0] != null)
	// curLocProgress[0].dismiss();
	// curLocProgress[0] = null;
	// }
	//
	// public void onLocationChanged(Location location) {
	// if (location != null) {
	// if (location.getAccuracy() < 40) {
	// locationManager.removeUpdates(this);
	// if (curLocProgress[0] != null)
	// curLocProgress[0].dismiss();
	// curLocProgress[0] = null;
	// }
	// LocationX loc = new LocationX(location);
	// if (view != null)
	// view.setMyLocation(loc, true);
	// }
	// }
	// };
	//
	// curLocProgress[0] = ProgressDialog.show(this, "",
	// "����������� ���������...", true, true, new OnCancelListener() {
	// public void onCancel(DialogInterface dialog) {
	// locationManager.removeUpdates(locListener);
	// curLocProgress[0] = null;
	// }
	// });
	//
	// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
	// 1, locListener);
	//
	// }

	private void addPlacemark() {
		if (view == null)
			return;
		final LocationX pm = view.createPlacemark();
		if (pm != null) {
			PlaceMarkDlg dlg = new PlaceMarkDlg(this, view, pm,
					mapsService.getPlacemarks(), PlaceMarkDlg.Type.ADD);
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
					mapsService.getPlacemarks(), PlaceMarkDlg.Type.EDIT);
			dlg.show();
		}

		if (sel instanceof PathSelection) {
			PathSelection pathSel = (PathSelection) sel;
			new PathDlg(this, pathSel.path, pathSel.pm, mapsService.getPaths())
					.show();
		}
	}

	private void trackingOnOff() {
		if (mapsService == null)
			return;
		if (mapsService.getTracker().isTracking()) {
			mapsService.getTracker().endPath();
		} else {
			mapsService.getTracker().startPath();
		}
		updateWritingButton();
		updateView();
	}

	private void updateWritingButton() {
		if (mapsService != null)
			findViewById(R.id.writing).setVisibility(
					mapsService.getTracker().isTracking() ? View.VISIBLE
							: View.GONE);
	}

	public boolean isKineticScrolling() {
		return settings == null
				|| settings.getBoolean(KINETIC_SCROLLING_SETTING, true);
	}

	public boolean loadDuringScrolling() {
		return settings == null
				|| settings.getBoolean(LOAD_DURING_SCROLLING_SETTING, true);
	}

	@Override
	protected void onDestroy() {
		if (view != null && mapsService != null) {
			Bundle b = mapsService.getBundle();
			view.save(b);
		}
		// locationManager = null;

		Adapter.log("onDestroy");

		if (mapsService != null) {
			unbindService(conn);
			disconnectFromService();
		}

		if (view != null)
			view.dispose();
		if (diagramView != null)
			diagramView.dispose();

		view = null;
		diagramView = null;

		adapter.recycle();
		adapter = null;

		super.onDestroy();
		System.runFinalizersOnExit(true);
		
		unbindDrawables(findViewById(R.id.RootView));
		
		System.gc();

	}

	private void unbindDrawables(View view) {
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
	        	// AdapterViews, ListViews and potentially other ViewGroups don�t support the removeAllViews operation
	        }
	    }
	}
	
	public void updateView() {
		if (view != null)
			view.invalidate();
	}

	public void pathSelected(PathSelection sel) {
		if (diagramView != null)
			diagramView.pathSelected(sel);
	}

}